package competition.subsystems.vision;

import org.photonvision.PhotonCameraExtended;

public interface SimpleCamera {
    public String getName();
    public PhotonCameraExtended getCamera();

    default boolean isCameraWorking() {
        return getCamera().doesLibraryVersionMatchCoprocessorVersion()
                && getCamera().isConnected();
    }
}
