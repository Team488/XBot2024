package competition.electrical_contract;

import javax.inject.Inject;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import competition.subsystems.pose.PoseSubsystem;
import xbot.common.injection.electrical_contract.CANTalonInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;

public class CompetitionContract extends ElectricalContract {

    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public CompetitionContract() {}

    @Override
    public CANTalonInfo getLeftLeader() {
        return new CANTalonInfo(1, true, FeedbackDevice.CTRE_MagEncoder_Absolute, true, simulationScalingValue);
    }

    @Override
    public CANTalonInfo getRightLeader() {
        return new CANTalonInfo(2, true, FeedbackDevice.CTRE_MagEncoder_Absolute, true, simulationScalingValue);
    }
    @Override
    public DeviceInfo getLightsDio0() {
        return new DeviceInfo(5);
    }

    @Override
    public DeviceInfo getLightsDio1() {
        return new DeviceInfo(6);
    }

    @Override
    public DeviceInfo getLightsDio2() {
        return new DeviceInfo(7);
    }

    @Override
    public DeviceInfo getLightsDio3() {
        return new DeviceInfo(8); // something on the navX, just out of the way
    }

    @Override
    public DeviceInfo getLightsDio4() {
        return new DeviceInfo(9); // something on the navX, just out of the way
    }

    @Override
    public DeviceInfo getLightsCubeDio() {
        return new DeviceInfo(4);
    }
}
