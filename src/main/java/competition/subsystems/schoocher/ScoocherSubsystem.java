package competition.subsystems.schoocher;

import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import competition.electrical_contract.ElectricalContract;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
@Singleton
public class ScoocherSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    public final ElectricalContract contract;
    public DoubleProperty sendingPower;
    public XCANSparkMax scoocherMotor;

    @Inject
    public ScoocherSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                             ElectricalContract electricalContract, XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        this.contract = electricalContract;

        if (contract.isScoocherReady()) {
            this.scoocherMotor = sparkMaxFactory.createWithoutProperties(contract.getScoocherMotor(), getPrefix(), "Scoocher");
        }
        pf.setPrefix(this);
        sendingPower = pf.createPersistentProperty("sendingPower", 1.0);
    }

    private void setPower(double power) {
        if (contract.isScoocherReady()) {
            scoocherMotor.set(power);
        }
    }

    public void intakeNote() {
        setPower(sendingPower.get());
    }
    public void ejectNote(){
        setPower(-sendingPower.get());
    }

    public void stop() {
        setPower(0);
    }

    @Override
    public void refreshDataFrame() {
        if (contract.isScoocherReady()) {
            scoocherMotor.refreshDataFrame();
        }
    }
}


