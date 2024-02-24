package competition.subsystems.vision;

import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.XbotPhotonPoseEstimator;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCameraExtended;
import org.photonvision.PhotonPoseEstimator;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.logic.TimeStableValidator;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
public class VisionSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    public static final String VISION_TABLE = "photonvision";

    public static final String TARGET_POSE = "forwardAprilCamera/targetPose";
    public static final String LATENCY_MILLIS = "forwardAprilCamera/latencyMillis";

    final PhotonCameraExtended frontLeftAprilCamera;
    final PhotonCameraExtended frontRightAprilCamera;
    final PhotonCameraExtended rearLeftAprilCamera;
    final PhotonCameraExtended rearRightAprilCamera;


    boolean frontLeftAprilCameraWorking = true;
    boolean frontRightAprilCameraWorking = true;
    boolean rearLeftAprilCameraWorking = true;
    boolean rearRightAprilCameraWorking = true;

    final RobotAssertionManager assertionManager;
    final BooleanProperty isInverted;
    final DoubleProperty yawOffset;
    final DoubleProperty waitForStablePoseTime;
    final DoubleProperty errorThreshold;
    final DoubleProperty singleTagStableDistance;
    final DoubleProperty multiTagStableDistance;
    final TimeStableValidator frontLeftReliablePoseIsStable;
    final TimeStableValidator frontRightReliablePoseIsStable;
    final TimeStableValidator rearLeftReliablePoseIsStable;
    final TimeStableValidator rearRightReliablePoseIsStable;
    NetworkTable visionTable;
    AprilTagFieldLayout aprilTagFieldLayout;
    XbotPhotonPoseEstimator customPhotonPoseEstimator;
    PhotonPoseEstimator frontLeftPhotonPoseEstimator;
    PhotonPoseEstimator frontRightPhotonPoseEstimator;
    PhotonPoseEstimator rearLeftPhotonPoseEstimator;
    PhotonPoseEstimator rearRightPhotonPoseEstimator;
    boolean aprilTagsLoaded = false;
    long logCounter = 0;

    @Inject
    public VisionSubsystem(PropertyFactory pf, RobotAssertionManager assertionManager) {
        this.assertionManager = assertionManager;
        visionTable = NetworkTableInstance.getDefault().getTable(VISION_TABLE);

        pf.setPrefix(this);
        isInverted = pf.createPersistentProperty("Yaw inverted", true);
        yawOffset = pf.createPersistentProperty("Yaw offset", 0);
        singleTagStableDistance = pf.createPersistentProperty("Single tag stable distance", 2.0);
        multiTagStableDistance = pf.createPersistentProperty("Multi tag stable distance", 4.0);


        waitForStablePoseTime = pf.createPersistentProperty("Pose stable time", 0.0, Property.PropertyLevel.Debug);
        errorThreshold = pf.createPersistentProperty("Error threshold",200);
        frontLeftReliablePoseIsStable = new TimeStableValidator(() -> waitForStablePoseTime.get());
        frontRightReliablePoseIsStable = new TimeStableValidator(() -> waitForStablePoseTime.get());
        rearLeftReliablePoseIsStable = new TimeStableValidator(() -> waitForStablePoseTime.get());
        rearRightReliablePoseIsStable = new TimeStableValidator(() -> waitForStablePoseTime.get());

        // TODO: Add resiliency to this subsystem, so that if the camera is not connected, it doesn't cause a pile
        // of errors. Some sort of VisionReady in the ElectricalContract may also make sense. Similarly,
        // we need to handle cases like not having the AprilTag data loaded.

        PhotonCameraExtended.setVersionCheckEnabled(false);
        frontLeftAprilCamera = new PhotonCameraExtended("Apriltag_FrontLeft_Camera");
        frontRightAprilCamera = new PhotonCameraExtended("Apriltag_FrontRight_Camera");
        rearLeftAprilCamera = new PhotonCameraExtended("Apriltag_RearLeft_Camera");
        rearRightAprilCamera = new PhotonCameraExtended("Apriltag_RearRight_Camera");

        // Check to see if we have incorrect versions. If so, then we need to not use that camera as the underlying libraries
        // could be unstable, leading to robot crashes or anomalous behavior.
        frontLeftAprilCameraWorking = isCameraWorking(frontLeftAprilCamera);
        frontRightAprilCameraWorking = isCameraWorking(frontRightAprilCamera);
        rearLeftAprilCameraWorking = isCameraWorking(rearLeftAprilCamera);
        rearRightAprilCameraWorking = isCameraWorking(rearRightAprilCamera);

        try {
            aprilTagFieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile);
            aprilTagsLoaded = true;
            log.info("Successfully loaded AprilTagFieldLayout");
        } catch (IOException e) {
            log.error("Could not load AprilTagFieldLayout!", e);
        }

        //Cam mounted 1" forward of center, 17" up, 12.5" right.
        Transform3d robotToFrontRightCam = new Transform3d(new Translation3d(
                13.48 / PoseSubsystem.INCHES_IN_A_METER,
                -13.09 / PoseSubsystem.INCHES_IN_A_METER,
                9.25 / PoseSubsystem.INCHES_IN_A_METER),
                new Rotation3d(0, Math.toRadians(30.5), Math.toRadians(-14)));
        /*Transform3d robotToFrontLeftCam = new Transform3d(new Translation3d(
                13.48 / PoseSubsystem.INCHES_IN_A_METER,
                13.09/ PoseSubsystem.INCHES_IN_A_METER,
                9.25 / PoseSubsystem.INCHES_IN_A_METER),
                new Rotation3d(0, Math.toRadians(30.5), Math.toRadians(14)));*/
        Transform3d robotToFrontLeftCam = new Transform3d(new Translation3d(
                0,0,0),
                new Rotation3d(0,0,0));
        Transform3d robotToRearRightCam = new Transform3d(new Translation3d(
                -13.48 / PoseSubsystem.INCHES_IN_A_METER,
                -13.09 / PoseSubsystem.INCHES_IN_A_METER,
                9.25 / PoseSubsystem.INCHES_IN_A_METER),
                new Rotation3d(0, Math.toRadians(30.5), Math.toRadians(180 + 14)));
        Transform3d robotToRearLeftCam = new Transform3d(new Translation3d(
                -13.48 / PoseSubsystem.INCHES_IN_A_METER,
                13.09 / PoseSubsystem.INCHES_IN_A_METER,
                9.25 / PoseSubsystem.INCHES_IN_A_METER),
                new Rotation3d(0, Math.toRadians(30.5), Math.toRadians(180 - 14)));


        frontLeftPhotonPoseEstimator = new PhotonPoseEstimator(
                aprilTagFieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                frontLeftAprilCamera,
                robotToFrontLeftCam
        );
        frontRightPhotonPoseEstimator = new PhotonPoseEstimator(
                aprilTagFieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                frontRightAprilCamera,
                robotToFrontRightCam
        );
        rearLeftPhotonPoseEstimator = new PhotonPoseEstimator(
                aprilTagFieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                rearLeftAprilCamera,
                robotToRearLeftCam
        );
        rearRightPhotonPoseEstimator = new PhotonPoseEstimator(
                aprilTagFieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                rearRightAprilCamera,
                robotToRearRightCam
        );
    }

    public Optional<EstimatedRobotPose>[] getPhotonVisionEstimatedPoses(Pose2d previousEstimatedRobotPose) {
        if (aprilTagsLoaded) {
            Optional<EstimatedRobotPose> frontLeftEstimatedPose = Optional.empty();
            Optional<EstimatedRobotPose> frontRightEstimatedPose = Optional.empty();
            Optional<EstimatedRobotPose> rearLeftEstimatedPose = Optional.empty();
            Optional<EstimatedRobotPose> rearRightEstimatedPose = Optional.empty();

            if (frontLeftAprilCameraWorking) {
                frontLeftEstimatedPose = getPhotonVisionEstimatedPose("FrontLeft", frontLeftPhotonPoseEstimator,
                        previousEstimatedRobotPose, frontLeftReliablePoseIsStable);
            }
            if (frontRightAprilCameraWorking) {
                frontRightEstimatedPose = getPhotonVisionEstimatedPose("FrontRight", frontRightPhotonPoseEstimator,
                        previousEstimatedRobotPose, frontRightReliablePoseIsStable);
            }
            if (rearLeftAprilCameraWorking) {
                rearLeftEstimatedPose = getPhotonVisionEstimatedPose("RearLeft", rearLeftPhotonPoseEstimator,
                        previousEstimatedRobotPose, rearLeftReliablePoseIsStable);
            }
            if (rearRightAprilCameraWorking) {
                rearRightEstimatedPose = getPhotonVisionEstimatedPose("RearRight", rearRightPhotonPoseEstimator,
                        previousEstimatedRobotPose, rearRightReliablePoseIsStable);
            }
            return new Optional[] {frontLeftEstimatedPose, frontRightEstimatedPose,
                    rearLeftEstimatedPose, rearRightEstimatedPose};
        } else {
            return new Optional[] {Optional.empty()};
        }
    }

    public Optional<EstimatedRobotPose> getPhotonVisionEstimatedPose(
            String name, PhotonPoseEstimator estimator, Pose2d previousEstimatedRobotPose, TimeStableValidator poseTimeValidator) {
        if (aprilTagsLoaded) {
            estimator.setReferencePose(previousEstimatedRobotPose);
            var estimatedPose = estimator.update();
            // Log the estimated pose, and log an insane value if we don't have one (so we don't clutter up the visualization)
            if (estimatedPose.isPresent())
            {
                aKitLog.record(name+"Estimate", estimatedPose.get().estimatedPose.toPose2d());
            }

            var isReliable = !estimatedPose.isEmpty() && isEstimatedPoseReliable(estimatedPose.get(), previousEstimatedRobotPose);
            aKitLog.record(name+"Reliable", isReliable);
            var isStable = waitForStablePoseTime.get() == 0.0 || poseTimeValidator.checkStable(isReliable);
            if (isReliable && isStable) {
                return estimatedPose;
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    /**
     * Performs several sanity checks on the estimated pose:
     * - Are we seeing an invalid tag?
     * - Are we pretty close to our previous location?
     * - If we see 2+ targets, we're good
     * - If we see 1 target, we need to be close and have a low ambiguity
     * @param estimatedPose The pose to check
     * @param previousEstimatedPose The previous location of the robot in the last loop
     * @return True if the pose is reliable and should be consumed by the robot
     */
    public boolean isEstimatedPoseReliable(EstimatedRobotPose estimatedPose, Pose2d previousEstimatedPose) {
        if (estimatedPose.targetsUsed.size() == 0) {
            return false;
        }

        // Pose isn't reliable if we see a tag id that shouldn't be on the field
        var allTagIds = getTagListFromPose(estimatedPose);
        if (allTagIds.stream().anyMatch(id -> id < 1 || id > 16)) {
            log.warn("Ignoring vision pose with invalid tag id. Visible tags: "
                    + getStringFromList(allTagIds));
            return false;
        }

        double distance = previousEstimatedPose.getTranslation().getDistance(estimatedPose.estimatedPose.toPose2d().getTranslation());
        if(distance > errorThreshold.get()) {
            if (logCounter++ % 20 == 0) {
                log.warn(String.format("Ignoring vision pose because distance is %f from our previous pose. Current pose: %s, vision pose: %s.",
                        distance,
                        previousEstimatedPose.getTranslation().toString(),
                        estimatedPose.estimatedPose.getTranslation().toString()));
            }
            return false;
        }

        // How far away is the camera from the target?
        double cameraDistance =
                estimatedPose.targetsUsed.get(0).getBestCameraToTarget().getTranslation().getNorm();

        // Two or more targets tends to be very reliable, but there's still a limit for distance
        if (estimatedPose.targetsUsed.size() > 1
        && cameraDistance < multiTagStableDistance.get()) {
            return true;
        }

        // For a single target we need to be above reliability threshold and within 1m
        return estimatedPose.targetsUsed.get(0).getPoseAmbiguity() < 0.20
                && cameraDistance < singleTagStableDistance.get();
    }

    private List<Integer> getTagListFromPose(EstimatedRobotPose estimatedPose) {
        return Arrays.asList(estimatedPose.targetsUsed.stream()
                .map(target -> target.getFiducialId()).toArray(Integer[]::new));
    }

    private String getStringFromList(List<Integer> list) {
        return String.join(", ", list.stream().mapToInt(id -> id).mapToObj(id -> Integer.toString(id)).toArray(String[]::new));
    }

    private boolean isCameraWorking(PhotonCameraExtended camera) {
        return camera.doesLibraryVersionMatchCoprocessorVersion();
    }

    int loopCounter = 0;

    @Override
    public void periodic() {
        loopCounter++;

        boolean anyFrontCameraBroken = !frontLeftAprilCameraWorking || !frontRightAprilCameraWorking;
        boolean anyRearCameraBroken = !rearLeftAprilCameraWorking || !rearRightAprilCameraWorking;
        // If one of the cameras is not working, see if they have self healed every 5 seconds
        if (loopCounter % (50 * 5) == 0 && (anyFrontCameraBroken || anyRearCameraBroken)) {
            log.info("Before check, Forward April camera working: " + frontLeftAprilCameraWorking
                    + ", Rear April camera working: " + frontRightAprilCameraWorking
                    + ", Left Rear April camera working: " + rearLeftAprilCameraWorking
                    + ", Right Rear April camera working: " + rearRightAprilCameraWorking);
            log.info("Checking if cameras have self healed");
            frontLeftAprilCameraWorking = isCameraWorking(frontLeftAprilCamera);
            frontRightAprilCameraWorking = isCameraWorking(frontRightAprilCamera);
            rearLeftAprilCameraWorking = isCameraWorking(rearLeftAprilCamera);
            rearRightAprilCameraWorking = isCameraWorking(rearRightAprilCamera);
            log.info("After check, Forward April camera working: " + frontLeftAprilCameraWorking
                    + ", Rear April camera working: " + frontRightAprilCameraWorking
                    + ", Left Rear April camera working: " + rearLeftAprilCameraWorking
                    + ", Right Rear April camera working: " + rearRightAprilCameraWorking);
        }

        aKitLog.record("ForwardAprilCameraWorking", frontLeftAprilCameraWorking);
        aKitLog.record("RearAprilCameraWorking", frontRightAprilCameraWorking);
        aKitLog.record("LeftRearAprilCameraWorking", rearLeftAprilCameraWorking);
        aKitLog.record("RightRearAprilCameraWorking", rearRightAprilCameraWorking);
    }

    @Override
    public void refreshDataFrame() {
        if (aprilTagsLoaded) {
            if (frontLeftAprilCameraWorking) {
                frontLeftAprilCamera.refreshDataFrame();
            }
            if (frontRightAprilCameraWorking) {
                frontRightAprilCamera.refreshDataFrame();
            }
            if (rearLeftAprilCameraWorking) {
                rearLeftAprilCamera.refreshDataFrame();
            }
            if (rearRightAprilCameraWorking) {
                rearRightAprilCamera.refreshDataFrame();
            }
        }
    }
}
