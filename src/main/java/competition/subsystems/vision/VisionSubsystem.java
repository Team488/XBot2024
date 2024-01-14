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
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.sensors.XPhotonCamera;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.logic.TimeStableValidator;
import xbot.common.math.XYPair;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.vision.XbotPhotonPoseEstimatorFork;

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

    final XPhotonCamera forwardAprilCamera;
    final XPhotonCamera rearAprilCamera;
    final RobotAssertionManager assertionManager;
    final BooleanProperty isInverted;
    final DoubleProperty yawOffset;
    final DoubleProperty waitForStablePoseTime;
    final DoubleProperty errorThreshold;
    final TimeStableValidator frontReliablePoseIsStable;
    final TimeStableValidator rearReliablePoseIsStable;
    NetworkTable visionTable;
    AprilTagFieldLayout aprilTagFieldLayout;
    XbotPhotonPoseEstimator customPhotonPoseEstimator;
    XbotPhotonPoseEstimatorFork photonPoseEstimator;
    XbotPhotonPoseEstimatorFork rearPhotonPoseEstimator;
    boolean visionWorking = false;
    long logCounter = 0;

    @Inject
    public VisionSubsystem(PropertyFactory pf, RobotAssertionManager assertionManager, XPhotonCamera.XPhotonCameraFactory cameraFactory) {

        // Temporary while waiting for PhotonVision to update and make this plausible
        forwardAprilCamera = cameraFactory.create("forwardAprilCamera");
        rearAprilCamera = cameraFactory.create("rearAprilCamera");

        this.assertionManager = assertionManager;
        visionTable = NetworkTableInstance.getDefault().getTable(VISION_TABLE);

        pf.setPrefix(this);
        isInverted = pf.createPersistentProperty("Yaw inverted", true);
        yawOffset = pf.createPersistentProperty("Yaw offset", 0);

        waitForStablePoseTime = pf.createPersistentProperty("Pose stable time", 0.0, Property.PropertyLevel.Debug);
        errorThreshold = pf.createPersistentProperty("Error threshold",200);
        frontReliablePoseIsStable = new TimeStableValidator(() -> waitForStablePoseTime.get());
        rearReliablePoseIsStable = new TimeStableValidator(() -> waitForStablePoseTime.get());

        // TODO: Add resiliency to this subsystem, so that if the camera is not connected, it doesn't cause a pile
        // of errors. Some sort of VisionReady in the ElectricalContract may also make sense. Similarly,
        // we need to handle cases like not having the AprilTag data loaded.

        try {
            aprilTagFieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile);
            visionWorking = true;
            log.info("Successfully loaded AprilTagFieldLayout");
        } catch (IOException e) {
            log.error("Could not load AprilTagFieldLayout!", e);
        }

        //Cam mounted 1" forward of center, 17" up, 12.5" right.
        Transform3d robotToCam = new Transform3d(new Translation3d(
                1.395 / PoseSubsystem.INCHES_IN_A_METER,
                -11.712 / PoseSubsystem.INCHES_IN_A_METER,
                16.421 / PoseSubsystem.INCHES_IN_A_METER),
                new Rotation3d(0, 0, Math.toRadians(7.595)));
        Transform3d robotToRearCam = new Transform3d(new Translation3d(
                -1.395 / PoseSubsystem.INCHES_IN_A_METER,
                11.712 / PoseSubsystem.INCHES_IN_A_METER,
                16.421 / PoseSubsystem.INCHES_IN_A_METER),
                new Rotation3d(0, 0, Math.toRadians(180 + 7.595)));
        /*customPhotonPoseEstimator = new XbotPhotonPoseEstimator(
            aprilTagFieldLayout, 
            XbotPhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
            forwardAprilCamera, 
            robotToCam);*/
        //customPhotonPoseEstimator.setMaximumPoseAmbiguityThreshold(0.2);
        photonPoseEstimator = new XbotPhotonPoseEstimatorFork(
                aprilTagFieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                forwardAprilCamera,
                robotToCam
        );
        rearPhotonPoseEstimator = new XbotPhotonPoseEstimatorFork(
                aprilTagFieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                rearAprilCamera,
                robotToRearCam
        );
    }

    public XYPair getAprilCoordinates() {

        var result = forwardAprilCamera.getLatestResult();
        if (!result.hasTargets()) {
            return null;
        }


        NetworkTable aprilTable = NetworkTableInstance.getDefault().getTable(VISION_TABLE);
        double[] targetPose = aprilTable.getEntry(TARGET_POSE).getDoubleArray(new double[0]);
        if (targetPose.length == 0) {
            return null;
        }
        if (Math.abs(targetPose[0]) < 0.01 && Math.abs(targetPose[1]) < 0.01) {
            return null;
        }
        return new XYPair(-targetPose[0], -targetPose[1]);
    }

    public Optional<EstimatedRobotPose> getPhotonVisionEstimatedPose(Pose2d previousEstimatedRobotPose) {
        if (visionWorking) {
            //customPhotonPoseEstimator.setReferencePose(previousEstimatedRobotPose);
            //return customPhotonPoseEstimator.update();
            photonPoseEstimator.setReferencePose(previousEstimatedRobotPose);
            var estimatedPose = photonPoseEstimator.update();
            var isReliable = !estimatedPose.isEmpty() && isEstimatedPoseReliable(estimatedPose.get(), previousEstimatedRobotPose);
            var isStable = waitForStablePoseTime.get() == 0.0 || frontReliablePoseIsStable.checkStable(isReliable);
            if (isReliable && isStable) {
                return estimatedPose;
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public Optional<EstimatedRobotPose> getRearPhotonVisionEstimatedPose(Pose2d previousEstimatedRobotPose) {
        if (visionWorking) {
            rearPhotonPoseEstimator.setReferencePose(previousEstimatedRobotPose);
            var estimatedPose = rearPhotonPoseEstimator.update();
            var isReliable = !estimatedPose.isEmpty() && isEstimatedPoseReliable(estimatedPose.get(), previousEstimatedRobotPose);
            var isStable = waitForStablePoseTime.get() == 0.0 || rearReliablePoseIsStable.checkStable(isReliable);
            if (isReliable && isStable) {
                return estimatedPose;
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public boolean isEstimatedPoseReliable(EstimatedRobotPose estimatedPose, Pose2d previousEstimatedPose) {
        if (estimatedPose.targetsUsed.size() == 0) {
            return false;
        }

        // Pose isn't reliable if we see a tag id that shouldn't be on the field
        var allTagIds = getTagListFromPose(estimatedPose);
        if (allTagIds.stream().anyMatch(id -> id < 1 || id > 8)) {
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

        if (logCounter++ % 20 == 0) {
            log.info(String.format("Estimated pose %s from tags %s. Previous pose was %s.",
                    estimatedPose.estimatedPose.toPose2d(), getStringFromList(allTagIds), previousEstimatedPose));
        }

        // Two or more targets tends to be very reliable
        if (estimatedPose.targetsUsed.size() > 1) {
            return true;
        }

        // For a single target we need to be above reliability threshold and within 1m
        return estimatedPose.targetsUsed.get(0).getPoseAmbiguity() < customPhotonPoseEstimator.getMaximumPoseAmbiguityThreshold()
                && estimatedPose.targetsUsed.get(0).getBestCameraToTarget().getTranslation().getX() < 1.5;
    }

    private List<Integer> getTagListFromPose(EstimatedRobotPose estimatedPose) {
        return Arrays.asList(estimatedPose.targetsUsed.stream()
                .map(target -> target.getFiducialId()).toArray(Integer[]::new));
    }

    private String getStringFromList(List<Integer> list) {
        return String.join(", ", list.stream().mapToInt(id -> id).mapToObj(id -> Integer.toString(id)).toArray(String[]::new));
    }

    @Override
    public void refreshDataFrame() {
        forwardAprilCamera.refreshDataFrame();
        rearAprilCamera.refreshDataFrame();
    }
}
