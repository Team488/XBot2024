package competition.subsystems.pose;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import org.littletonrobotics.junction.Logger;
import org.photonvision.EstimatedRobotPose;
import xbot.common.controls.sensors.XGyro.XGyroFactory;
import xbot.common.logic.Latch;
import xbot.common.logic.TimeStableValidator;
import xbot.common.math.FieldPose;
import xbot.common.math.WrappedRotation2d;
import xbot.common.math.XYPair;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;


import java.util.Optional;


@Singleton
public class PoseSubsystem extends BasePoseSubsystem {

    private final DriveSubsystem drive;
    final SwerveDrivePoseEstimator fusedSwerveOdometry;
    final SwerveDrivePoseEstimator onlyWheelsGyroSwerveOdometry;
    private final VisionSubsystem vision;
    protected Optional<DriverStation.Alliance> cachedAlliance;

    private TimeStableValidator noSurprisingDistanceRequests = new TimeStableValidator(1);
    private final DoubleProperty suprisingVisionUpdateDistanceInMetersProp;
    private boolean isPoseHealthy;
    private final BooleanProperty useForwardCameraForPose;
    private final BooleanProperty useRearCameraForPose;
    private TimeStableValidator extremelyConfidentVisionValidator = new TimeStableValidator(10);
    private final DoubleProperty extremelyConfidentVisionDistanceUpdateInMetersProp;
    private final boolean isVisionPoseExtremelyConfident;
    private final BooleanProperty allianceAwareFieldProp;
    private final BooleanProperty useVisionForPoseProp;
    private final Latch useVisionToUpdateGyroLatch;
    public final static Translation2d BLUE_SPEAKER_POSITION = new Translation2d(-0.0381,5.547868);
    public final static Translation2d RED_SPEAKER_POSITION = new Translation2d(16.579342,5.547868);

    private DoubleProperty matchTime;

    @Inject
    public PoseSubsystem(XGyroFactory gyroFactory, PropertyFactory propManager, DriveSubsystem drive, VisionSubsystem vision) {
        super(gyroFactory, propManager);
        this.drive = drive;
        this.vision = vision;

        suprisingVisionUpdateDistanceInMetersProp = propManager.createPersistentProperty("SuprisingVisionUpdateDistanceInMeters", 0.5);
        isPoseHealthy = false;
        extremelyConfidentVisionDistanceUpdateInMetersProp = propManager.createPersistentProperty("ExtremelyConfidentVisionDistanceUpdateInMeters", 0.01);
        isVisionPoseExtremelyConfident = false;
        allianceAwareFieldProp = propManager.createPersistentProperty("Alliance Aware Field", true);
        useVisionForPoseProp = propManager.createPersistentProperty("Enable Vision-Assisted Pose", true);
        useForwardCameraForPose = propManager.createPersistentProperty("Use forward april cam", true);
        useRearCameraForPose = propManager.createPersistentProperty("Use rear april cam", true);

        // In the DriveSubsystem, the swerve modules were initialized in this order:
        // FrontLeft, FrontRight, RearLeft, RearRight.
        // When initializing SwerveDriveOdometry, we need to use the same order.

        fusedSwerveOdometry = initializeSwerveOdometry();
        onlyWheelsGyroSwerveOdometry = initializeSwerveOdometry();

        useVisionToUpdateGyroLatch = new Latch(false, Latch.EdgeType.RisingEdge, edge -> {
           if (edge== Latch.EdgeType.RisingEdge) {
               log.info("Vision has been so confident for so long that we are force-updating our overall pose.");
               this.setCurrentPoseInMeters(getVisionAssistedPositionInMeters());
           }
        });
    }

    private SwerveDrivePoseEstimator initializeSwerveOdometry() {
        return new SwerveDrivePoseEstimator(
            drive.getSwerveDriveKinematics(),
            getCurrentHeading(),
            new SwerveModulePosition[] {
                drive.getFrontLeftSwerveModuleSubsystem().getCurrentPosition(),
                drive.getFrontRightSwerveModuleSubsystem().getCurrentPosition(),
                drive.getRearLeftSwerveModuleSubsystem().getCurrentPosition(),
                drive.getRearRightSwerveModuleSubsystem().getCurrentPosition()
            },
            new Pose2d());
    }

    /**
     * Update alliance from driver station, typically done during init
     */
    public void updateAllianceFromDriverStation() { this.cachedAlliance = DriverStation.getAlliance();}

    /**
     * Gets whether the robot should behave with an alliance-aware field.
     * i.e. Should the field origin always be on the blue alliance side of the field?
     * @return Whether the robot pose is alliance-aware.
     */
    public boolean isAllianceAwareField() { return this.allianceAwareFieldProp.get(); }

    public void setAllianceAwareField(boolean isAllianceAware) { this.allianceAwareFieldProp.set(isAllianceAware); }

    /**
     * Gets whether the robot should consider vision values for calculating pose.
     * @return Whether the robot pose will be calculated using vision.
     */
    public boolean isUsingVisionAssistedPose() { return this.useVisionForPoseProp.get(); }

    /**
     * This is a legacy method for tank drive robots, and does not apply to swerve. We should look at
     * updating the base class to remove/replace this method.
     */
    @Override
    protected double getLeftDriveDistance() {
        return drive.getLeftTotalDistance();
    }

    /**
     * This is a legacy method for tank drive robots, and does not apply to swerve. We should look at
     * updating the base class to remove/replace this method.
     */
    @Override
    protected double getRightDriveDistance() {
        return drive.getRightTotalDistance();
    }

    @Override
    protected void updateOdometry() {
        // The swerve modules return units in meters, which is what the swerve odometry expects.
        // In principle the input/output here is unitless, but we're using meters internally for any calculations
        // while still presenting inches externally to dashboards.

        // Update the basic odometry (gyro, encoders)
        Pose2d updatedPosition = fusedSwerveOdometry.update(
                this.getCurrentHeading(),
                getSwerveModulePositions()
        );

        Pose2d updatedPositionWheelsGyroOnly = onlyWheelsGyroSwerveOdometry.update(
                this.getCurrentHeading(),
                getSwerveModulePositions()
        );

        if (isUsingVisionAssistedPose()) {
            improveFusedOdometryUsingPhotonLib(updatedPosition);
        }

        Logger.recordOutput(getPrefix()+"VisionEstimate", fusedSwerveOdometry.getEstimatedPosition());
        Logger.recordOutput(getPrefix()+"WheelsOnlyEstimate", onlyWheelsGyroSwerveOdometry.getEstimatedPosition());

        // Pull out the new estimated pose from odometry. Note that for now, we only pull out X and Y
        // and trust the gyro implicitly. Eventually, we should allow the gyro to be updated via vision
        // if we have a lot of confidence in the vision data.
        var estimatedPosition = new Pose2d(
                fusedSwerveOdometry.getEstimatedPosition().getTranslation(),
                getCurrentHeading());

        totalDistanceX = estimatedPosition.getX();
        totalDistanceY = estimatedPosition.getY();

        // Convert back to inches
        double prevTotalDistanceX = totalDistanceX;
        double prevTotalDistanceY = totalDistanceY;
        Logger.recordOutput(this.getPrefix()+"RobotPose", estimatedPosition);

        this.velocityX = ((totalDistanceX - prevTotalDistanceX));
        this.velocityY = ((totalDistanceY - prevTotalDistanceY));
        this.totalVelocity = (Math.sqrt(Math.pow(velocityX, 2.0) + Math.pow(velocityY, 2.0)));
    }

    public void copyFusedOdometryToWheelsOdometry() {
        onlyWheelsGyroSwerveOdometry.resetPosition(
            fusedSwerveOdometry.getEstimatedPosition().getRotation(),
            getSwerveModulePositions(),
            fusedSwerveOdometry.getEstimatedPosition());
    }

    public Command createCopyFusedOdometryToWheelsOdometryCommand() {
        return Commands.runOnce(() -> copyFusedOdometryToWheelsOdometry());
    }

    public Command createSetPositionCommand(Pose2d pose) {
        return Commands.runOnce(() -> setCurrentPosition(pose));
    }

    private void improveFusedOdometryUsingPhotonLib(Pose2d recentPosition) {
        var photonEstimatedPoses = vision.getPhotonVisionEstimatedPoses(recentPosition);

        boolean appliedAnyVisionData = false;
        for (var photonEstimatedPose : photonEstimatedPoses) {
            appliedAnyVisionData |= applyVisionDataIfAppropriate(photonEstimatedPose, recentPosition);
        }

        if (!appliedAnyVisionData) {
            // Since we didn't get any vision updates, by definition we didn't get any surprising distance updates.
            isPoseHealthy = noSurprisingDistanceRequests.checkStable(true);
        }
    }

    private boolean applyVisionDataIfAppropriate(Optional<EstimatedRobotPose> photonEstimatedPose, Pose2d recentPosition) {
        if (photonEstimatedPose.isPresent()) {
            // Get the result data, which has both coordinates and timestamps
            var camPose = photonEstimatedPose.get();

            // Check for the distance delta between the old and new poses. If it's too large, reset
            // the healthy pose validator.
            double distance = recentPosition.getTranslation().getDistance(recentPosition.getTranslation());
            boolean isSurprisingDistance = (distance > suprisingVisionUpdateDistanceInMetersProp.get());
            isPoseHealthy = noSurprisingDistanceRequests.checkStable(isSurprisingDistance);

            // In any case, update the odometry with the new pose from the camera.
            fusedSwerveOdometry.addVisionMeasurement(camPose.estimatedPose.toPose2d(), camPose.timestampSeconds);
            return true;
        }
        return false;
    }

    public boolean getIsPoseHealthy() {
        return isPoseHealthy;
    }

    public boolean getIsVisionPoseExtremelyConfident() {
        return isVisionPoseExtremelyConfident;
    }

    public Pose2d getVisionAssistedPositionInMeters() {
        return fusedSwerveOdometry.getEstimatedPosition();
    }

    public void setCurrentPosition(double newXPositionMeters, double newYPositionMeters, WrappedRotation2d heading) {
        super.setCurrentPosition(newXPositionMeters, newYPositionMeters);
        super.setCurrentHeading(heading.getDegrees());
        fusedSwerveOdometry.resetPosition(
            heading,
            getSwerveModulePositions(),
            new Pose2d(
                newXPositionMeters,
                newYPositionMeters,
                this.getCurrentHeading()));

        copyFusedOdometryToWheelsOdometry();
    }

    public void setCurrentPosition(Pose2d pose) {
        setCurrentPosition(pose.getTranslation().getX(), pose.getTranslation().getY(), WrappedRotation2d.fromRotation2d(pose.getRotation()));
    }

    @Override
    public void setCurrentPosition(double newXPosition, double newYPosition) {
        setCurrentPosition(newXPosition, newYPosition, this.getCurrentHeading());
    }

    public void setCurrentPose(FieldPose newPose) {
        setCurrentPosition(newPose.getPoint().x, newPose.getPoint().y, newPose.getHeading());
        this.setCurrentHeading(newPose.getHeading().getDegrees());
    }

    public void setCurrentPoseInMeters(Pose2d newPoseInMeters) {
        setCurrentPosition(
            newPoseInMeters.getTranslation().getX(),
            newPoseInMeters.getTranslation().getY(),
            WrappedRotation2d.fromRotation2d(newPoseInMeters.getRotation())
        );
    }


    private SwerveModulePosition[] getSwerveModulePositions() {
        return new SwerveModulePosition[] {
            drive.getFrontLeftSwerveModuleSubsystem().getCurrentPosition(),
            drive.getFrontRightSwerveModuleSubsystem().getCurrentPosition(),
            drive.getRearLeftSwerveModuleSubsystem().getCurrentPosition(),
            drive.getRearRightSwerveModuleSubsystem().getCurrentPosition()
        };
    }

    public XYPair rotateVelocityBasedOnAlliance() {
        if (DriverStation.getAlliance().equals(DriverStation.Alliance.Red)) {
            return getCurrentVelocity().clone().scale(-1);
        } else {
            return getCurrentVelocity();
        }
    }

    // We actually need something simpler to work with the velocity program - robot oriented X velocity.
    // moing forwards is positive, moving backwards is negative.
    // We can get the robot oriented velocity by adjusting for the current orientation.
    public double getRobotOrientedXVelocity() {
        var currentVelocityVector = getCurrentVelocity();
        // Let's say the robot was facing -90 degrees (field-relative) (aka "south")
        // and was driving in that direction. This would be a vector like 0, -1.
        // We need to rotate that so it would be 1, 0. That suggests we need to rotate that vector
        // by 90 degrees; the negative of our current heading.
        var robotOrientedVelocityVector = currentVelocityVector.clone().rotate(-getCurrentHeading().getDegrees());
        // now just get the x component
        return robotOrientedVelocityVector.x;
    }

    public DoubleProperty getMatchTime(){
        return matchTime;
    }

    @Override
    public void periodic() {
        super.periodic();
        Logger.recordOutput(getPrefix()+"PoseHealthy", isPoseHealthy);
        Logger.recordOutput(getPrefix()+"VisionPoseExtremelyConfident", isVisionPoseExtremelyConfident);
    }

}