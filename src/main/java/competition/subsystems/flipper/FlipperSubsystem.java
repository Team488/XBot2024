package competition.subsystems.flipper;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XSolenoid;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FlipperSubsystem extends BaseSubsystem {

    final XSolenoid flipperSolenoid;
    private boolean active;

    @Inject
    public FlipperSubsystem(XSolenoid.XSolenoidFactory xSolenoidFactory, ElectricalContract contract,
                            PropertyFactory pf) {
        pf.setPrefix(this);

        this.flipperSolenoid = xSolenoidFactory.create(contract.getFlipperSolenoid().channel);

        active = false;
    }

    public void setSolenoid() {
        flipperSolenoid.setOn(active);
    }

    public void solenoidActive() {
        active = true;
        setSolenoid();
    }

    public void solenoidInactive() {
        active = false;
        setSolenoid();
    }

    public boolean getActive() {
        return active;
    }
}
