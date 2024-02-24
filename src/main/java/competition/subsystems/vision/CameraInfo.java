package competition.subsystems.vision;

import edu.wpi.first.math.geometry.Transform3d;

public record CameraInfo(String networkTablesName, String friendlyName, Transform3d position) {
}
