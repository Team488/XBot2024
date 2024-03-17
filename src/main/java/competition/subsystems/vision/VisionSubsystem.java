package competition.subsystems.vision;

import competition.electrical_contract.CompetitionContract;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCameraExtended;
import org.photonvision.PhotonPoseEstimator;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.logic.TimeStableValidator;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.vision.AprilTagCamera;
import xbot.common.subsystems.vision.CameraCapabilities;
import xbot.common.subsystems.vision.SimpleCamera;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class VisionSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    final RobotAssertionManager assertionManager;
    final BooleanProperty isInverted;
    final DoubleProperty yawOffset;
    final DoubleProperty waitForStablePoseTime;
    final DoubleProperty robotDisplacementThresholdToRejectVisionUpdate;
    final DoubleProperty singleTagStableDistance;
    final DoubleProperty multiTagStableDistance;
    final DoubleProperty maxNoteRatio;
    final DoubleProperty minNoteRatio;
    final DoubleProperty minNoteConfidence;
    AprilTagFieldLayout aprilTagFieldLayout;
    final ArrayList<AprilTagCamera> aprilTagCameras;
    final ArrayList<NoteCamera> noteCameras;
    final ArrayList<SimpleCamera> allCameras;
    boolean aprilTagsLoaded = false;
    long logCounter = 0;
    Pose3d[] detectedNotes;
    NoteTracker[] noteTrackers;
    final DoubleProperty noteLocalizationInfo;
    NoteCamera rearLeftNoteCamera;
    NoteCamera rearRightNoteCamera;
    NoteCamera rearCenterNoteCamera;


    @Inject
    public VisionSubsystem(PropertyFactory pf, XCameraElectricalContract electricalContract, RobotAssertionManager assertionManager) {
        this.assertionManager = assertionManager;

        pf.setPrefix(this);
        isInverted = pf.createPersistentProperty("Yaw inverted", true);
        yawOffset = pf.createPersistentProperty("Yaw offset", 0);
        singleTagStableDistance = pf.createPersistentProperty("Single tag stable distance", 2.0);
        multiTagStableDistance = pf.createPersistentProperty("Multi tag stable distance", 4.0);
        maxNoteRatio = pf.createPersistentProperty("Max note size ratio", 5.5);
        minNoteRatio = pf.createPersistentProperty("Min note size ratio", 2.0);
        minNoteConfidence = pf.createPersistentProperty("Min note confidence", 0.8);

        var trackingNt = NetworkTableInstance.getDefault().getTable("SmartDashboard");
        var detectionTopicNames = new String[]{
                //"DetectionCameraphotonvisionfrontleft/Target Coordinate pairs",
                //"DetectionCameraphotonvisionfrontright/Target Coordinate pairs",
                "DetectionCameraphotonvisionrearleft/Target Coordinate pairs",
                "DetectionCameraphotonvisionrearright/Target Coordinate pairs",
                "DetectionCameraxbot-orin-nano-1/Target Coordinate pairs"
        };
        noteTrackers = Arrays.stream(detectionTopicNames)
                .map(NoteTracker::new)
                .toArray(NoteTracker[]::new);

        waitForStablePoseTime = pf.createPersistentProperty("Pose stable time", 0.0, Property.PropertyLevel.Debug);
        robotDisplacementThresholdToRejectVisionUpdate = pf.createPersistentProperty("Displacement Threshold to reject Vision (m)",3);

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
            var aprilTagCapableCameras = Arrays
                    .stream(electricalContract.getCameraInfo())
                    .filter(info -> info.capabilities().contains(CameraCapabilities.APRIL_TAG))
                    .toArray(CameraInfo[]::new);
            for (var camera : aprilTagCapableCameras) {
                aprilTagCameras.add(new AprilTagCamera(camera, waitForStablePoseTime::get, aprilTagFieldLayout, this.getPrefix()));
            }
        }

        noteCameras = new ArrayList<NoteCamera>();
        var noteTrackingCapableCameras = Arrays
                .stream(electricalContract.getCameraInfo())
                .filter(info -> info.capabilities().contains(CameraCapabilities.GAME_SPECIFIC))
                .toArray(CameraInfo[]::new);
        for (var camera : noteTrackingCapableCameras) {
            NoteCamera noteCamera = new NoteCamera(camera, this.getPrefix());
            noteCameras.add(noteCamera);
            if (noteCamera.getName().equals(CompetitionContract.rearRightNoteCameraName)){
                rearRightNoteCamera = noteCamera;
            }
            if (noteCamera.getName().equals(CompetitionContract.rearLeftNoteCameraName)){
                rearLeftNoteCamera = noteCamera;
            }
            if (noteCamera.getName().equals(CompetitionContract.rearCenterNoteCameraName)){
                rearCenterNoteCamera = noteCamera;
            }
        }

        allCameras = new ArrayList<>();
        allCameras.addAll(aprilTagCameras);
        allCameras.addAll(noteCameras);

        pf.setPrefix("NoteLocalizationInfo/");
        noteLocalizationInfo = pf.createPersistentProperty("ScalingFactor", 0.8);
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
        // No targets, so there's no way we should use this data.
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

        // If this is way too far from our current location, then it's probably a bad estimate.
        double distance = previousEstimatedPose.getTranslation().getDistance(estimatedPose.estimatedPose.toPose2d().getTranslation());
        if(distance > robotDisplacementThresholdToRejectVisionUpdate.get()) {
            if (logCounter++ % 20 == 0) {
                log.warn(String.format("Ignoring vision pose because distance is %f from our previous pose. Current pose: %s, vision pose: %s.",
                        distance,
                        previousEstimatedPose.getTranslation().toString(),
                        estimatedPose.estimatedPose.getTranslation().toString()));
            }
            return false;
        }

        // How far away is the camera from the target?
        double cameraDistanceToTarget =
                estimatedPose.targetsUsed.get(0).getBestCameraToTarget().getTranslation().getNorm();

        // Two or more targets tends to be very reliable, but there's still a limit for distance
        if (estimatedPose.targetsUsed.size() > 1
        && cameraDistanceToTarget < multiTagStableDistance.get()) {
            return true;
        }

        // For a single target we need to be above reliability threshold and very close.
        return estimatedPose.targetsUsed.get(0).getPoseAmbiguity() < 0.20
                && cameraDistanceToTarget < singleTagStableDistance.get();
    }

    private List<Integer> getTagListFromPose(EstimatedRobotPose estimatedPose) {
        return Arrays.asList(estimatedPose.targetsUsed.stream()
                .map(target -> target.getFiducialId()).toArray(Integer[]::new));
    }

    private String getStringFromList(List<Integer> list) {
        return String.join(", ", list.stream().mapToInt(id -> id).mapToObj(id -> Integer.toString(id)).toArray(String[]::new));
    }

    int loopCounter = 0;

    public double getNoteYaw(NoteCamera camera) {
        var result = camera.getCamera().getLatestResult();
        if (result == null) {
            return 0;
        }

        var targets = result.getTargets();
        if (targets == null || targets.size() == 0) {
            return 0;
        }

        return targets.get(0).getYaw();
    }

    public double getNoteArea(NoteCamera camera) {
        var result = camera.getCamera().getLatestResult();
        if (result == null) {
            return -1;
        }

        var targets = result.getTargets();
        if (targets == null || targets.size() == 0) {
            return -1;
        }
        return targets.get(0).getArea();
    }

    public Pose3d[] getDetectedNotes() {
        return detectedNotes;
    }

    public double getNoteYawFromCentralCamera() {
        return getNoteYaw(rearCenterNoteCamera);
    }

    private Translation2d triangulateNote() {
        double rearRightyaw = getNoteYaw(rearRightNoteCamera);
        double rearLeftYaw = getNoteYaw(rearLeftNoteCamera);

        if (Math.abs(rearRightyaw) < 0.001 || Math.abs(rearLeftYaw) < 0.001) {
            return null;
        }

        // At this point, we have a note that was detected by both cameras. We should be able to triangulate the note
        // just based on the two yaws and the known positions & angles of the cameras

        double distanceBetweenCameras = 12.853*2 / PoseSubsystem.INCHES_IN_A_METER;
        //each camera is pointed at a relative 125 degrees to the line connecting them
        double leftCameraAdjusted = 35 + rearLeftYaw;
        double rightCameraAdjusted = 35 - rearRightyaw;
        double noteAngle = 180 - leftCameraAdjusted - rightCameraAdjusted;

        // Now we can apply the law of sines, where a = b * (sin(A)/(sin(B))
        // Let's use the rear left camera as our A, so we can find the length between rearRight and the target
        double leftCameraRads = Math.toRadians(leftCameraAdjusted);
        double rightCameraRads = Math.toRadians(rightCameraAdjusted);
        double sideLengthOppositeFromRearLeft = distanceBetweenCameras * (Math.sin(leftCameraRads) / Math.sin(noteAngle));

        // rear left, when the note was on the right, saw positive values.
        // That suggests that camear


        return null;
    }

    @Override
    public void periodic() {
        loopCounter++;

        var anyCameraBroken = allCameras.stream().anyMatch(state -> !state.isCameraWorking());

        // If one of the cameras is not working, see if they have self healed every 5 seconds
        if (loopCounter % (50 * 5) == 0 && (anyCameraBroken)) {
            log.info("Checking if cameras have self healed");
            for (SimpleCamera camera : aprilTagCameras) {
                if (!camera.isCameraWorking()) {
                    log.info("Camera " + camera.getName() + " is still not working");
                }
            }
        }

        for (SimpleCamera camera : allCameras) {
            aKitLog.record(camera.getName() + "CameraWorking", camera.isCameraWorking());
        }

        for (NoteCamera camera : noteCameras) {
            if (camera.isCameraWorking()) {
                aKitLog.record(camera.getName() + "NoteYaw", getNoteYaw(camera));
                aKitLog.record(camera.getName() + "NoteArea", getNoteArea(camera));
            }
        }

        Arrays.stream(noteTrackers)
                .forEach(NoteTracker::refreshDataFrame);
        var detections = Arrays.stream(noteTrackers)
                .map(NoteTracker::getDetections)
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
        detectedNotes = Arrays.stream(detections)
                .map(detection -> {
                    var parts = detection.split(",");
                    var ratio = Double.parseDouble(parts[3]);
                    var confidence = Double.parseDouble(parts[4]);
                    if (confidence < minNoteConfidence.get()) {
                        return null;
                    }
                    if (ratio > maxNoteRatio.get() || ratio < minNoteRatio.get()) {
                        return null;
                    }
                    return new Pose3d(Double.parseDouble(parts[0]) / PoseSubsystem.INCHES_IN_A_METER,
                            Double.parseDouble(parts[1]) / PoseSubsystem.INCHES_IN_A_METER,
                            Double.parseDouble(parts[2]) / PoseSubsystem.INCHES_IN_A_METER,
                            new Rotation3d()
                    );
                })
                .filter(Objects::nonNull)
                .toArray(Pose3d[]::new);

        aKitLog.record("DetectedNotes", detectedNotes);
    }

    @Override
    public void refreshDataFrame() {
        if (aprilTagsLoaded) {
            for (SimpleCamera camera : allCameras) {
                camera.getCamera().refreshDataFrame();
            }
        }
    }
}
