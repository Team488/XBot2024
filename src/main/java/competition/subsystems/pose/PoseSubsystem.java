package competition.subsystems.pose;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import xbot.common.controls.sensors.XGyro.XGyroFactory;
import xbot.common.controls.sensors.XTimer;
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


import competition.subsystems.drive.DriveSubsystem;
import xbot.common.controls.sensors.XGyro.XGyroFactory;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import edu.wpi.first.wpilibj.DriverStation;


@Singleton
public class PoseSubsystem extends BasePoseSubsystem {

    private final DriveSubsystem drive;
    final SwerveDrivePoseEstimator swerveOdometry;
    private final VisionSubsystem vision;
    private final Field2d fieldForDisplay;
    protected DriverStation.Alliance cachedAlliance = DriverStation.Alliance.Invalid;

    private TimeStableValidator healthyPoseValidator = new TimeStableValidator(1);
    private final DoubleProperty suprisingVisionUpdateDistanceInMetersProp;
    private final BooleanProperty isPoseHealthyProp;
    private final BooleanProperty useForwardCameraForPose;
    private final BooleanProperty useRearCameraForPose;
    private TimeStableValidator extremelyConfidentVisionValidator = new TimeStableValidator(10);
    private final DoubleProperty extremelyConfidentVisionDistanceUpdateInMetersProp;
    private final BooleanProperty isVisionPoseExtremelyConfidentProp;
    private final BooleanProperty allianceAwareFieldProp;
    private final BooleanProperty useVisionForPoseProp;
    private final Latch useVisionToUpdateGyroLatch;

    private DoubleProperty matchTime;

    @Inject
    public PoseSubsystem(XGyroFactory gyroFactory, PropertyFactory propManager, DriveSubsystem drive, VisionSubsystem vision) {
        super(gyroFactory, propManager);
        this.drive = drive;
        this.vision = vision;

        suprisingVisionUpdateDistanceInMetersProp = propManager.createPersistentProperty("SuprisingVisionUpdateDistanceInMeters", 0.5);
        isPoseHealthyProp = propManager.createEphemeralProperty("IsPoseHealthy", false);
        extremelyConfidentVisionDistanceUpdateInMetersProp = propManager.createPersistentProperty("ExtremelyConfidentVisionDistanceUpdateInMeters", 0.01);
        isVisionPoseExtremelyConfidentProp = propManager.createEphemeralProperty("IsVisionPoseExtremelyConfident", false);
        allianceAwareFieldProp = propManager.createPersistentProperty("Alliance Aware Field", true);
        useVisionForPoseProp = propManager.createPersistentProperty("Enable Vision-Assisted Pose", true);
        useForwardCameraForPose = propManager.createPersistentProperty("Use forward april cam", true);
        useRearCameraForPose = propManager.createPersistentProperty("Use rear april cam", true);

        // TODO: This is a hack to get the field visualization working. Eventually this is going to cause problems
        // once there are test cases that try and invoke the PoseSubsystem. Right now, the SmartDashboardCommandPutter
        // is the only class we have that manages direct calls to SmartDashboard. Maybe we should broaden it to a more
        // generic "XSmartDashboard" class for scenarios like these?
        fieldForDisplay = new Field2d();
        SmartDashboard.putData("Field", fieldForDisplay);

        // In the DriveSubsystem, the swerve modules were initialized in this order:
        // FrontLeft, FrontRight, RearLeft, RearRight.
        // When initializing SwerveDriveOdometry, we need to use the same order.

        /*swerveOdometry = new SwerveDrivePoseEstimator(
            drive.getSwerveDriveKinematics(), 
            getCurrentHeading(), 
            new SwerveModulePosition[] {
                drive.getFrontLeftSwerveModuleSubsystem().getcurrentPosition(),
                drive.getFrontRightSwerveModuleSubsystem().getcurrentPosition(),
                drive.getRearLeftSwerveModuleSubsystem().getcurrentPosition(),
                drive.getRearRightSwerveModuleSubsystem().getcurrentPosition()
            },
            new Pose2d());*/

        useVisionToUpdateGyroLatch = new Latch(false, Latch.EdgeType.RisingEdge, edge -> {
           if (edge== Latch.EdgeType.RisingEdge) {
               log.info("Vision has been so confident for so long that we are force-updating our overall pose.");
               this.setCurrentPoseInMeters(getVisionAssistedPositionInMeters());
           }
        });
        // creating matchtime doubleProperty
        matchTime = propManager.createEphemeralProperty("Time", DriverStation.getMatchTime());

    }

    /**
     * Update alliance from driver station, typically done during init
     */
    public void updateAllianceFromDriverStation() { this.cachedAlliance = DriverStation.getAlliance();}

    /**
     * Gets the robot's alliance color
     * @return The robot alliance color
     */
    public DriverStation.Alliance getAlliance() {
        return DriverStation.getAlliance();
    }

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
     * Rotate the vector by 180 degrees if the driver is on the red alliance.
     * @param vector The vector value.
     * @return The rotated input.
     */
    public XYPair rotateVectorBasedOnAlliance(XYPair vector) {
        if (getAlliance() == DriverStation.Alliance.Red && isAllianceAwareField()) {
            vector.scale(-1, -1);
        }
        return vector;
    }

    /**
     * Rotate the angle by 180 degrees if the driver is on the red alliance.
     * @param rotation The angle to rotate.
     * @return The rotated input.
     */
    public Rotation2d rotateAngleBasedOnAlliance(Rotation2d rotation) {
        var alliance = getAlliance();
        log.info("Detected Alliance:" + alliance + ", and AllianceAwareField is:" +allianceAwareFieldProp.get());

        if (getAlliance() == DriverStation.Alliance.Red && isAllianceAwareField()) {
            log.info("Detected red alliance and AllianceAwareField. Rotating angle 180 degrees.");
            return Rotation2d.fromDegrees(rotation.getDegrees() - (rotation.getDegrees() - 90.0) * 2);
        }
        return rotation;
    }

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
        Pose2d updatedPosition = swerveOdometry.update(
                this.getCurrentHeading(),
                getSwerveModulePositions()
        );

        if (isUsingVisionAssistedPose()) {
            // As a prototype, consider any AprilTag seen to be at field coordinates 0,0. Use that information
            // to position the robot on the field.
            //improveOdometryUsingSimpleAprilTag();

            // As a better prototype, use PhotonLib to evaluate multiple AprilTags and get a field-accurate position.
            improveOdometryUsingPhotonLib(updatedPosition);

            // TODO: as an even better prototype, use a multi-target PNP solver (not yet available, but coming soon?)
        }

        // Pull out the new estimated pose from odometry. Note that for now, we only pull out X and Y
        // and trust the gyro implicitly. Eventually, we should allow the gyro to be updated via vision
        // if we have a lot of confidence in the vision data.
        var estimatedPosition = new Pose2d(
                swerveOdometry.getEstimatedPosition().getTranslation(),
                getCurrentHeading());

        // Convert back to inches
        double prevTotalDistanceX = totalDistanceX.get();
        double prevTotalDistanceY = totalDistanceY.get();
        totalDistanceX.set(estimatedPosition.getX() * PoseSubsystem.INCHES_IN_A_METER);
        totalDistanceY.set(estimatedPosition.getY() * PoseSubsystem.INCHES_IN_A_METER);
        fieldForDisplay.setRobotPose(estimatedPosition);
        this.velocityX.set((totalDistanceX.get() - prevTotalDistanceX));
        this.velocityY.set((totalDistanceY.get() - prevTotalDistanceY));
        this.totalVelocity.set(Math.sqrt(Math.pow(velocityX.get(), 2.0) + Math.pow(velocityY.get(), 2.0)));
    }

    private void improveOdometryUsingSimpleAprilTag() {
        // Try to get some vision sauce in there
        // and feed it straight into the odometry, then do the shifting at the very end when we convert back to inches.
        XYPair aprilCoords = vision.getAprilCoordinates();
        if (aprilCoords != null) {
            Pose2d aprilPos = new Pose2d(aprilCoords.x, aprilCoords.y, getCurrentHeading());
            swerveOdometry.addVisionMeasurement(aprilPos, XTimer.getFPGATimestamp() - 0.030);
        }
    }

    private void improveOdometryUsingPhotonLib(Pose2d recentPosition) {
        var photonEstimatedPose = vision.getPhotonVisionEstimatedPose(recentPosition);
        var rearPhotonEstimatedPose = vision.getRearPhotonVisionEstimatedPose(recentPosition);

        var updatedPoseWithVision = false;
        var poseConfident = true;
        if (photonEstimatedPose.isPresent()) {
            // Get the result data, which has both coordinates and timestamps
            var camPose = photonEstimatedPose.get();

            // Check for the distance delta between the old and new poses. If it's too large, reset
            // the healthy pose validator.
            double distance = recentPosition.getTranslation().getDistance(recentPosition.getTranslation());
            boolean isSurprisingDistance = (distance > suprisingVisionUpdateDistanceInMetersProp.get());
            isPoseHealthyProp.set(healthyPoseValidator.checkStable(isSurprisingDistance));

            // If the distance is really, really small, we're extremely confident in the vision data, and
            // could consider using it to update the gyro.
            poseConfident &= distance < extremelyConfidentVisionDistanceUpdateInMetersProp.get();

            // In any case, update the odometry with the new pose from the camera.
            swerveOdometry.addVisionMeasurement(camPose.estimatedPose.toPose2d(), camPose.timestampSeconds);
            updatedPoseWithVision = true;
        }

        if (rearPhotonEstimatedPose.isPresent()) {
            // Get the result data, which has both coordinates and timestamps
            var camPose = rearPhotonEstimatedPose.get();

            // Check for the distance delta between the old and new poses. If it's too large, reset
            // the healthy pose validator.
            double distance = recentPosition.getTranslation().getDistance(recentPosition.getTranslation());
            boolean isSurprisingDistance = (distance > suprisingVisionUpdateDistanceInMetersProp.get());
            isPoseHealthyProp.set(healthyPoseValidator.checkStable(isSurprisingDistance));

            // If the distance is really, really small, we're extremely confident in the vision data, and
            // could consider using it to update the gyro.
            poseConfident &= distance < extremelyConfidentVisionDistanceUpdateInMetersProp.get();

            // In any case, update the odometry with the new pose from the camera.
            swerveOdometry.addVisionMeasurement(camPose.estimatedPose.toPose2d(), camPose.timestampSeconds);
            updatedPoseWithVision = true;
        }

        if (updatedPoseWithVision) {
            isVisionPoseExtremelyConfidentProp.set(extremelyConfidentVisionValidator.checkStable(poseConfident));
        } else {
            // Since we didn't get any vision updates, we assume the current pose is healthy.
            isPoseHealthyProp.set(healthyPoseValidator.checkStable(true));
            // But since we didn't get any vision updates, we can't be super-confident!
            isVisionPoseExtremelyConfidentProp.set(extremelyConfidentVisionValidator.checkStable(false));
        }
    }

    public boolean getIsPoseHealthy() {
        return isPoseHealthyProp.get();
    }

    public boolean getIsVisionPoseExtremelyConfident() {
        return isVisionPoseExtremelyConfidentProp.get();
    }

    public Pose2d getVisionAssistedPositionInMeters() {
        return swerveOdometry.getEstimatedPosition();
    }

    public void setCurrentPosition(double newXPosition, double newYPosition, WrappedRotation2d heading) {
        super.setCurrentPosition(newXPosition, newYPosition);
        super.setCurrentHeading(heading.getDegrees());
        swerveOdometry.resetPosition(
            heading,
            getSwerveModulePositions(),
            new Pose2d(
                newXPosition / PoseSubsystem.INCHES_IN_A_METER, 
                newYPosition / PoseSubsystem.INCHES_IN_A_METER, 
                this.getCurrentHeading()));
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
            newPoseInMeters.getTranslation().getX() * PoseSubsystem.INCHES_IN_A_METER,
            newPoseInMeters.getTranslation().getY() * PoseSubsystem.INCHES_IN_A_METER,
            WrappedRotation2d.fromRotation2d(newPoseInMeters.getRotation())
        );
    }


    /*private SwerveModulePosition[] getSwerveModulePositions() {
        return new SwerveModulePosition[] {
            drive.getFrontLeftSwerveModuleSubsystem().getcurrentPosition(),
            drive.getFrontRightSwerveModuleSubsystem().getcurrentPosition(),
            drive.getRearLeftSwerveModuleSubsystem().getcurrentPosition(),
            drive.getRearRightSwerveModuleSubsystem().getcurrentPosition()
        };
    }*/

    public XYPair rotateVelocityBasedOnAlliance() {
        if (DriverStation.getAlliance()== DriverStation.Alliance.Red) {
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
        matchTime.set(DriverStation.getMatchTime());
    }

}