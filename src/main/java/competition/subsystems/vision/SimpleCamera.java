package competition.subsystems.vision;

import org.photonvision.PhotonCameraExtended;

public interface SimpleCamera {
    public String getName();
    public boolean isCameraWorking();
    public PhotonCameraExtended getCamera();
}
