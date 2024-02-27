package competition.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import org.photonvision.PhotonCameraExtended;
import org.photonvision.PhotonPoseEstimator;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.logic.TimeStableValidator;

import java.util.function.Supplier;

public class AprilTagCamera implements SimpleCamera {
    private final PhotonPoseEstimator poseEstimator;

    private final PhotonCameraExtended camera;

    private final String friendlyName;

    private final TimeStableValidator isStable;

    public AprilTagCamera(CameraInfo cameraInfo,
                          Supplier<Double> poseStableTime,
                          AprilTagFieldLayout fieldLayout) {
        this.camera = new PhotonCameraExtended(cameraInfo.networkTablesName());
        this.friendlyName = cameraInfo.friendlyName();
        this.poseEstimator = new PhotonPoseEstimator(fieldLayout,
                PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                this.camera,
                cameraInfo.position());
        this.isStable = new TimeStableValidator(poseStableTime);
    }

    public String getName() {
        return this.friendlyName;
    }

    public boolean isCameraWorking() {
        return this.camera.doesLibraryVersionMatchCoprocessorVersion()
                && this.camera.isConnected();
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
