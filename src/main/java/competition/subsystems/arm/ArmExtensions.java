package competition.subsystems.arm;

import edu.wpi.first.util.struct.StructSerializable;

public class ArmExtensions implements StructSerializable {
    public double leftExtensionMm;
    public double rightExtensionMm;

    public ArmExtensions(double leftExtensionMm, double rightExtensionMm) {
        this.leftExtensionMm = leftExtensionMm;
        this.rightExtensionMm = rightExtensionMm;
    }

    public ArmExtensions(double extension) {
        this(extension, extension);
    }

    public ArmExtensions() {
        this(0, 0);
    }

    public boolean representsZero() {
        return Math.abs(leftExtensionMm) < 0.001 && Math.abs(rightExtensionMm) < 0.001;
    }

    public static final ArmExtensionsStruct struct = new ArmExtensionsStruct();
}
