package competition.subsystems.shooter;

import edu.wpi.first.util.struct.Struct;
import java.nio.ByteBuffer;

public class ShooterWheelTargetSpeedsStruct implements Struct<ShooterWheelTargetSpeeds> {

    @Override
    public Class<ShooterWheelTargetSpeeds> getTypeClass() {
        return ShooterWheelTargetSpeeds.class;
    }

    @Override
    public String getTypeString() {
        return "struct:ShooterWheelTargetSpeeds";
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
    public ShooterWheelTargetSpeeds unpack(ByteBuffer bb) {
        double upperWheelsTargetRPM = bb.getDouble();
        double lowerWheelsTargetRPM = bb.getDouble();
        return new ShooterWheelTargetSpeeds(upperWheelsTargetRPM, lowerWheelsTargetRPM);
    }

    @Override
    public void pack(ByteBuffer bb, ShooterWheelTargetSpeeds value) {
        bb.putDouble(value.upperWheelsTargetRPM);
        bb.putDouble(value.lowerWheelsTargetRPM);
    }
}
