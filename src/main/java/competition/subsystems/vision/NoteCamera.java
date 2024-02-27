package competition.subsystems.vision;

import org.photonvision.PhotonCameraExtended;
import xbot.common.injection.electrical_contract.CameraInfo;

public class NoteCamera implements SimpleCamera{
    private final PhotonCameraExtended camera;
    private final String friendlyName;

    public NoteCamera(CameraInfo cameraInfo) {
        this.camera = new PhotonCameraExtended(cameraInfo.networkTablesName());
        this.friendlyName = cameraInfo.friendlyName();
    }

    public String getName() {
        return this.friendlyName;
    }

    public PhotonCameraExtended getCamera() {
        return this.camera;
    }
}
