package competition.electrical_contract;

import javax.inject.Inject;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import competition.subsystems.pose.PoseSubsystem;
import xbot.common.injection.electrical_contract.CANTalonInfo;

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
}
