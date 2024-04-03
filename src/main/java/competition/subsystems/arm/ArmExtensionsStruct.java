package competition.subsystems.arm;

import edu.wpi.first.util.struct.Struct;
import java.nio.ByteBuffer;

public class ArmExtensionsStruct implements Struct<ArmExtensions> {

    @Override
    public Class<ArmExtensions> getTypeClass() {
        return ArmExtensions.class;
    }

    @Override
    public String getTypeString() {
        return "struct:ArmExtensions";
    }

    @Override
    public int getSize() {
        return kSizeDouble * 2;
    }

    @Override
    public String getSchema() {
        return "double upperWheelsTargetRPM;double lowerWheelsTargetRPM";
    }

    @Override
    public ArmExtensions unpack(ByteBuffer bb) {
        double upperWheelsTargetRPM = bb.getDouble();
        double lowerWheelsTargetRPM = bb.getDouble();
        return new ArmExtensions(upperWheelsTargetRPM, lowerWheelsTargetRPM);
    }

    @Override
    public void pack(ByteBuffer bb, ArmExtensions value) {
        bb.putDouble(value.leftExtensionMm);
        bb.putDouble(value.rightExtensionMm);
    }
}
