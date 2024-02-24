package competition.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Transform3d;
import org.photonvision.PhotonCameraExtended;
import org.photonvision.PhotonPoseEstimator;
import xbot.common.logic.TimeStableValidator;

import java.util.function.Supplier;

public class AprilTagCamera {
    private final PhotonPoseEstimator poseEstimator;

    private final PhotonCameraExtended camera;

    private final String friendlyName;

    private final TimeStableValidator isStable;

    public AprilTagCamera(String cameraName,
                          String friendlyName,
                          Supplier<Double> poseStableTime,
                          Transform3d cameraPosition,
                          AprilTagFieldLayout fieldLayout) {
        this.camera = new PhotonCameraExtended(cameraName);
        this.friendlyName = friendlyName;
        this.poseEstimator = new PhotonPoseEstimator(fieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                this.camera,
                cameraPosition);
        this.isStable = new TimeStableValidator(poseStableTime);
    }

    public String getName() {
        return this.friendlyName;
    }

    public boolean isCameraWorking() {
        return this.camera.doesLibraryVersionMatchCoprocessorVersion();
    }

    public PhotonCameraExtended getCamera() {
        return this.camera;
    }

    public PhotonPoseEstimator getPoseEstimator() {
        return this.poseEstimator;
    }

    public TimeStableValidator getIsStableValidator() {
        return isStable;
    }
}
