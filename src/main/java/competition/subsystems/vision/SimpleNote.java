package competition.subsystems.vision;

import edu.wpi.first.util.struct.StructSerializable;

public class SimpleNote implements StructSerializable {
    final double area;
    final double yaw;

    public SimpleNote(double area, double yaw) {
        this.area = area;
        this.yaw = yaw;
    }

    public double getArea() {
        return area;
    }

    public double getYaw() {
        return yaw;
    }
}
