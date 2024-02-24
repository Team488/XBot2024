package competition.subsystems.vision;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
public class VisionSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    final RobotAssertionManager assertionManager;
    final BooleanProperty isInverted;
    final DoubleProperty yawOffset;
    final DoubleProperty waitForStablePoseTime;
    final DoubleProperty errorThreshold;
    final DoubleProperty singleTagStableDistance;
    final DoubleProperty multiTagStableDistance;
    AprilTagFieldLayout aprilTagFieldLayout;
    final ArrayList<AprilTagCamera> aprilTagCameras;
    boolean aprilTagsLoaded = false;
    long logCounter = 0;

    @Inject
    public VisionSubsystem(PropertyFactory pf, ElectricalContract electricalContract, RobotAssertionManager assertionManager) {
        this.assertionManager = assertionManager;

        pf.setPrefix(this);
        isInverted = pf.createPersistentProperty("Yaw inverted", true);
        yawOffset = pf.createPersistentProperty("Yaw offset", 0);
        singleTagStableDistance = pf.createPersistentProperty("Single tag stable distance", 2.0);
        multiTagStableDistance = pf.createPersistentProperty("Multi tag stable distance", 4.0);


        waitForStablePoseTime = pf.createPersistentProperty("Pose stable time", 0.0, Property.PropertyLevel.Debug);
        errorThreshold = pf.createPersistentProperty("Error threshold",200);

        // TODO: Add resiliency to this subsystem, so that if the camera is not connected, it doesn't cause a pile
        // of errors. Some sort of VisionReady in the ElectricalContract may also make sense. Similarly,
        // we need to handle cases like not having the AprilTag data loaded.

        try {
            aprilTagFieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile);
            aprilTagsLoaded = true;
            log.info("Successfully loaded AprilTagFieldLayout");
        } catch (IOException e) {
            log.error("Could not load AprilTagFieldLayout!", e);
        }

        aprilTagCameras = new ArrayList<AprilTagCamera>();
        if (aprilTagsLoaded) {
            PhotonCameraExtended.setVersionCheckEnabled(false);
            for (var cameraInfo : electricalContract.getCameraInfo()) {
                aprilTagCameras.add(new AprilTagCamera(cameraInfo, waitForStablePoseTime::get, aprilTagFieldLayout));
            }
        }
    }

    public List<Optional<EstimatedRobotPose>> getPhotonVisionEstimatedPoses(Pose2d previousEstimatedRobotPose) {
        var estimatedPoses = new ArrayList<Optional<EstimatedRobotPose>>();
        for (AprilTagCamera cameraState : this.aprilTagCameras) {
            if (cameraState.isCameraWorking()) {
                var estimatedPose = getPhotonVisionEstimatedPose(cameraState.getName(), cameraState.getPoseEstimator(),
                        previousEstimatedRobotPose, cameraState.getIsStableValidator());
                estimatedPoses.add(estimatedPose);
            }
        }
        return estimatedPoses;
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

            var isReliable = estimatedPose.isPresent() && isEstimatedPoseReliable(estimatedPose.get(), previousEstimatedRobotPose);
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

    int loopCounter = 0;

    @Override
    public void periodic() {
        loopCounter++;

        var anyCameraBroken = aprilTagCameras.stream().anyMatch(state -> !state.isCameraWorking());

        // If one of the cameras is not working, see if they have self healed every 5 seconds
        if (loopCounter % (50 * 5) == 0 && (anyCameraBroken)) {
            log.info("Checking if cameras have self healed");
            for (AprilTagCamera state : aprilTagCameras) {
                if (!state.isCameraWorking()) {
                    log.info("Camera " + state.getName() + " is still not working");
                }
            }
        }

        for (AprilTagCamera state : aprilTagCameras) {
            aKitLog.record(state.getName() + "CameraWorking", state.isCameraWorking());
        }
    }

    @Override
    public void refreshDataFrame() {
        if (aprilTagsLoaded) {
            for (AprilTagCamera state : aprilTagCameras) {
                if (state.isCameraWorking()) {
                    state.getCamera().refreshDataFrame();
                }
            }
        }
    }
}
