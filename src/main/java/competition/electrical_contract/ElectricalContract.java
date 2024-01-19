package competition.electrical_contract;


import xbot.common.injection.electrical_contract.DeviceInfo;

public abstract class ElectricalContract {


    public abstract boolean isShooterReady();
    public DeviceInfo getShooterMotorFollower() {
        return new DeviceInfo(27, true);
    }

    public DeviceInfo getShooterMotorLeader() {
        return new DeviceInfo(32, true);
    }
}
