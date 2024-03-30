package competition.subsystems.vision;

import edu.wpi.first.util.struct.StructSerializable;

public class SimpleNote implements StructSerializable {
    final double area;
    final double yaw;
    final double pitch;

    public SimpleNote(double area, double yaw, double pitch) {
        this.area = area;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getArea() {
        return area;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() { return pitch; }
}
