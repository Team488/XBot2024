package competition.subsystems.vision;

import org.photonvision.PhotonCameraExtended;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.subsystems.vision.SimpleCamera;

public class NoteCamera extends SimpleCamera {

    public NoteCamera(CameraInfo cameraInfo) {
        super(cameraInfo);
    }

    public String getName() {
        return this.friendlyName;
    }

    public PhotonCameraExtended getCamera() {
        return this.camera;
    }
}
