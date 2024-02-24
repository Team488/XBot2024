package competition.subsystems.shooter;

import edu.wpi.first.util.struct.StructSerializable;

public class ShooterWheelTargetSpeeds implements StructSerializable {
    public double upperWheelsTargetRPM;
    public double lowerWheelsTargetRPM;

    public ShooterWheelTargetSpeeds(double upperWheelsTargetRPM, double lowerWheelsTargetRPM) {
        this.upperWheelsTargetRPM = upperWheelsTargetRPM;
        this.lowerWheelsTargetRPM = lowerWheelsTargetRPM;
    }

    public ShooterWheelTargetSpeeds(double rpmForUpperAndLower) {
        this(rpmForUpperAndLower, rpmForUpperAndLower);
    }

    public ShooterWheelTargetSpeeds() {
        this(0, 0);
    }

    public boolean representsZeroSpeed() {
        return Math.abs(upperWheelsTargetRPM) < 0.001 && Math.abs(lowerWheelsTargetRPM) < 0.001;
    }

    public static final ShooterWheelTargetSpeedsStruct struct = new ShooterWheelTargetSpeedsStruct();
}
