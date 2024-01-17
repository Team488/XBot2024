package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANTalonInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;

public abstract class ElectricalContract {
    public abstract CANTalonInfo getLeftLeader();
    public abstract CANTalonInfo getRightLeader();
    public abstract DeviceInfo getLightsDio0();
    public abstract DeviceInfo getLightsDio1();
    public abstract DeviceInfo getLightsDio2();
    public abstract DeviceInfo getLightsDio3();
    public abstract DeviceInfo getLightsDio4();
    public abstract DeviceInfo getLightsCubeDio();
}
