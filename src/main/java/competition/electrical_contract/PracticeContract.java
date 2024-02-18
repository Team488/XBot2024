package competition.electrical_contract;

import xbot.common.injection.electrical_contract.DeviceInfo;

import javax.inject.Inject;

public class PracticeContract extends CompetitionContract {
    @Inject
    public PracticeContract() {}

    public DeviceInfo getCollectorMotor() {
        return new DeviceInfo("Collector", 25,true);
    }
}
