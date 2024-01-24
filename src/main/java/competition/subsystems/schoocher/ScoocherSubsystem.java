package competition.subsystems.schoocher;

import xbot.common.command.BaseSubsystem;
import competition.electrical_contract.ElectricalContract;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
@Singleton
public class ScoocherSubsystem extends BaseSubsystem {

    public final ElectricalContract contract;
    public DoubleProperty sendingPower;
    public final XCANSparkMax scoocherMotor;


    @Inject
    public ScoocherSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                             ElectricalContract electricalContract, XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        this.contract = electricalContract;
        this.scoocherMotor = sparkMaxFactory.create(contract.getScoocherMotor(), getPrefix(), "ScoocherMotor", null);

        pf.setPrefix(this);
        sendingPower = pf.createPersistentProperty("sendingPower", 0.1);

    }



    public void intakeNote() {
        scoocherMotor.set(sendingPower.get());
    }
    public void ejectingNote(){
        scoocherMotor.set(-sendingPower.get());
    }

    public void stop() {
        scoocherMotor.set(0);
    }
}


