package competition.subsystems.pose;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
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
import java.sql.Driver;
import java.util.Optional;
import java.util.function.Supplier;


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

    public static final  Translation2d SPEAKER_POSITION = new Translation2d(-0.0381,5.547868);
    public static final Pose2d SPEAKER_AIM_TARGET = new Pose2d(0, 5.5, Rotation2d.fromDegrees(180));
    public static Pose2d BlueSpikeTop = new Pose2d(2.8956, 7.0012, new Rotation2d());
    public static Pose2d BlueSpikeMiddle = new Pose2d(2.8956, 5.5478, new Rotation2d());
    public static Pose2d BlueSpikeBottom = new Pose2d(2.8956, 4.1056, new Rotation2d());
    public static Pose2d CenterLine1 = new Pose2d(fieldXMidpointInMeters, 7.4584, new Rotation2d());
    public static Pose2d CenterLine2 = new Pose2d(fieldXMidpointInMeters, 5.7820, new Rotation2d());
    public static Pose2d CenterLine3 = new Pose2d(fieldXMidpointInMeters, 4.1056, new Rotation2d());
    public static Pose2d CenterLine4 = new Pose2d(fieldXMidpointInMeters, 2.4292, new Rotation2d());
    public static Pose2d CenterLine5 = new Pose2d(fieldXMidpointInMeters, 0.7528, new Rotation2d());

    public static Pose2d NearbySource = new Pose2d(14, 1.2, Rotation2d.fromDegrees(0));

    public static double closeColumnWidth = 0.254;
    public static double farColumnWidths = 0.3469;
    public static Translation2d BlueLeftStageColumn = new Translation2d(3.34, 4.122);
    public static Translation2d BlueTopStageColumn = new Translation2d(5.58, 5.42);
    public static Translation2d BlueBottomStageColumn = new Translation2d(5.58,  2.82);

    public static double SubwooferWidth = 0.95;
    public static double SubwooferHeight = 1.1;
    public static Translation2d BlueSubwoofer = new Translation2d(0.415, 5.55);

    public static Pose2d BlueAmpScoringLocation = new Pose2d(1.83, 7.71, Rotation2d.fromDegrees(90));

    // TODO: get good positions
    public static Pose2d BlueSubwooferTopScoringLocation = new Pose2d(0.751, 6.702, Rotation2d.fromDegrees(-120));
    public static Pose2d BlueSubwooferMiddleScoringLocation = new Pose2d(1.383, 5.54, Rotation2d.fromDegrees(180));
    public static Pose2d BlueSubwooferBottomScoringLocation = new Pose2d(0.758, 4.395, Rotation2d.fromDegrees(120));
    public static Pose2d BluePodiumScoringLocation = new Pose2d(2.770, 4.389, Rotation2d.fromDegrees(159));
    public static Pose2d BlueFarAmpScoringLocation = new Pose2d(3.073, 7.597, Rotation2d.fromDegrees(-146.6));
    public static Pose2d BlueBottomSpikeCloserToSpeakerScoringLocation = new Pose2d(2.237,4.355, Rotation2d.fromDegrees(0));
    public static Pose2d BlueTopSpikeCloserToSpeakerScoringLocation = new Pose2d(2.239,6.738, Rotation2d.fromDegrees(-153));
    public static Pose2d BlueOneRobotAwayFromCenterSubwooferScoringLocation = new Pose2d(2.312,5.561, Rotation2d.fromDegrees(180));
    public static Pose2d BlueSpikeTopWhiteLine = new Pose2d(1.93294, 7.0012, new Rotation2d());
    public static Pose2d BlueSpikeBottomWhiteLine = new Pose2d(1.93294, 4.1056, new Rotation2d());

    public static Rotation2d FaceCollectorToBlueSource = Rotation2d.fromDegrees(120);

    // More navigation points
    public static Pose2d BluePodiumWaypoint = new Pose2d(2.5, 4.25, new Rotation2d());
    public static Pose2d BlueTopWingUpper = new Pose2d(5.861, 7.374, new Rotation2d());
    public static Pose2d BlueTopWingLower = new Pose2d(5.861, 6.413, new Rotation2d());
    public static Pose2d BlueBottomWing = new Pose2d(5.861, 1.505, new Rotation2d());

    // Navigation in/under/around the stage
    public static Pose2d BlueStageNW = new Pose2d(4.347, 4.912, new Rotation2d());
    public static Pose2d BlueStageE = new Pose2d(5.886, 4.132, new Rotation2d());
    public static Pose2d BlueStageSW = new Pose2d(4.420, 3.239, new Rotation2d());
    public static Pose2d BlueStageCenter = new Pose2d(4.892, 4.081, new Rotation2d());
    public static Pose2d BlueSouthOfStage = new Pose2d(4.572, 2.069, new Rotation2d());

    // Slots for the source
    public static Pose2d BlueSourceNearest = new Pose2d(14.883, 0.684, Rotation2d.fromDegrees(120));
    public static Pose2d BlueSourceMiddle = new Pose2d(15.437, 0.967, Rotation2d.fromDegrees(120));
    public static Pose2d BlueSourceFarthest = new Pose2d(15.918, 1.276, Rotation2d.fromDegrees(120));

    // Hanging positions
    public static Pose2d BlueStageNWHangingPreparationPoint = new Pose2d(3.859, 5.573, Rotation2d.fromDegrees(-60));
    public static Pose2d BlueStageSWHangingPreparationPoint = new Pose2d(4.071, 2.736, Rotation2d.fromDegrees(60));
    public static Pose2d BlueStageEHangingPreparationPoint = new Pose2d(6.712, 4.087, Rotation2d.fromDegrees(180));

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
        useVisionForPoseProp = propManager.createPersistentProperty("Enable Vision-Assisted Pose", false);
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
    public boolean isUsingVisionAssistedPose() {
        return this.useVisionForPoseProp.get() || DriverStation.isTeleop();
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

    public Pose2d getNearestGoodScoringPosition() {
        //Gets current location in the form of X and Y coordinates
        Pose2d currentPose = getCurrentPose2d();

        // Gets distance between our current location and the good scoring locations
        double distanceSubwooferTopScoringPosition = PoseSubsystem.convertBlueToRedIfNeeded(BlueSubwooferTopScoringLocation)
                .getTranslation().getDistance(currentPose.getTranslation());
        double distanceSubwooferMiddleScoringPosition = PoseSubsystem.convertBlueToRedIfNeeded
                (BlueSubwooferMiddleScoringLocation).getTranslation().getDistance(currentPose.getTranslation());
        double distanceSubwooferBottomScoringPosition = PoseSubsystem.convertBlueToRedIfNeeded
                (BlueSubwooferBottomScoringLocation).getTranslation().getDistance(currentPose.getTranslation());

        double distanceSpikeTop = PoseSubsystem.convertBlueToRedIfNeeded(BlueSpikeTop).getTranslation()
                .getDistance(currentPose.getTranslation());
        double distanceSpikeMiddle = PoseSubsystem.convertBlueToRedIfNeeded(BlueSpikeMiddle).getTranslation()
                .getDistance(currentPose.getTranslation());
        double distancePodium = PoseSubsystem.convertBlueToRedIfNeeded(BluePodiumScoringLocation).getTranslation()
                .getDistance(currentPose.getTranslation());

        Pose2d closestGoodScoringPosition = BlueSubwooferTopScoringLocation;
        double leastDistance = distanceSubwooferTopScoringPosition;

        if (distanceSubwooferMiddleScoringPosition < leastDistance) {
            leastDistance = distanceSubwooferMiddleScoringPosition;
            closestGoodScoringPosition = BlueSubwooferMiddleScoringLocation;
        }

        if (distanceSubwooferBottomScoringPosition < leastDistance) {
            leastDistance = distanceSubwooferBottomScoringPosition;
            closestGoodScoringPosition = BlueSubwooferBottomScoringLocation;
        }

        if (distanceSpikeTop < leastDistance) {
            leastDistance = distanceSpikeTop;
            closestGoodScoringPosition = new Pose2d(
                    BlueSpikeTop.getTranslation(),
                    Rotation2d.fromDegrees(-153.64394)
                    );
        }

        if (distanceSpikeMiddle < leastDistance) {
            leastDistance = distanceSpikeMiddle;
            closestGoodScoringPosition = new Pose2d(
                    BlueSpikeMiddle.getTranslation(),
                    Rotation2d.fromDegrees(180)
                    );
        }

        if (distancePodium < leastDistance) {
            closestGoodScoringPosition = BluePodiumScoringLocation;
        }

        return closestGoodScoringPosition;
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

        improveFusedOdometryUsingPhotonLib(updatedPosition);

        aKitLog.record("VisionEstimate", fusedSwerveOdometry.getEstimatedPosition());
        aKitLog.record("WheelsOnlyEstimate", onlyWheelsGyroSwerveOdometry.getEstimatedPosition());

        Translation2d positionSource;
        if (isUsingVisionAssistedPose()) {
            // If we trust vision implicitly, use it all the time.
            // If we don't trust it strongly, we still need to use it in teleop as
            // odometry will build up a lot of error quickly.
            positionSource = fusedSwerveOdometry.getEstimatedPosition().getTranslation();
        } else {
            // If we don't trust vision, use only odometry. Typically happens in autonomous.
            positionSource = onlyWheelsGyroSwerveOdometry.getEstimatedPosition().getTranslation();
        }

        var estimatedPosition = new Pose2d(
                positionSource,
                getCurrentHeading());

        totalDistanceX = estimatedPosition.getX();
        totalDistanceY = estimatedPosition.getY();

        // Convert back to inches
        double prevTotalDistanceX = totalDistanceX;
        double prevTotalDistanceY = totalDistanceY;
        aKitLog.record("RobotPose", estimatedPosition);

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

    public Command createSetPositionCommand(Supplier<Pose2d> poseSupplier) {
        return Commands.runOnce(() -> setCurrentPosition(poseSupplier.get())).ignoringDisable(true);
    }

    public Command createSetPositionCommandThatMirrorsIfNeeded(Pose2d bluePose) {
        return Commands.runOnce(() -> setCurrentPosition(PoseSubsystem.convertBlueToRedIfNeeded(bluePose))).ignoringDisable(true);
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

    public double getDistanceFromSpeaker(){
        double distanceFromSpeakerInMeters;

        distanceFromSpeakerInMeters = getCurrentPose2d().getTranslation().getDistance(
                PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_POSITION));
        return distanceFromSpeakerInMeters;
    }

    public double getAngularErrorToSpeakerInDegrees() {
        return getAngularErrorToTranslation2dInDegrees(PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_POSITION), new Rotation2d());
    }

    public double getAngularErrorToTranslation2dInDegrees(Translation2d targetPosition, Rotation2d offset) {
        var angleFromChassisToTarget = WrappedRotation2d.fromDegrees(
                targetPosition.minus(getCurrentPose2d().getTranslation()).getAngle().rotateBy(offset).getDegrees());
        return getCurrentHeading().minus(angleFromChassisToTarget).getDegrees();
    }

    @Override
    public void periodic() {
        super.periodic();
        aKitLog.record("PoseHealthy", isPoseHealthy);
        aKitLog.record("VisionPoseExtremelyConfident", isVisionPoseExtremelyConfident);
        aKitLog.record("DistanceToSpeaker", getDistanceFromSpeaker());
    }
}



