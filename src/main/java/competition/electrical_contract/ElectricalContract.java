package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANTalonInfo;

public abstract class ElectricalContract {
    public abstract CANTalonInfo getLeftLeader();
    public abstract CANTalonInfo getRightLeader();
}
