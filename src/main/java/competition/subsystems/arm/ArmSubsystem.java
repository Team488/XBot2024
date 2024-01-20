package competition.subsystems.arm;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.math.MathUtils;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ArmSubsystem extends BaseSubsystem {

    public final XCANSparkMax armMotorLeft;
    public final XCANSparkMax armMotorRight;

    public ArmState armState;

    public DoubleProperty extendPower;
    public DoubleProperty retractPower;
    private DoubleProperty setPowerMax;
    private DoubleProperty setPowerMin;

    public enum ArmState {
        EXTENDING,
        RETRACTING,
        STOPPED
    }

    @Inject
    public ArmSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                        ElectricalContract contract) {

        pf.setPrefix(this);

        extendPower = pf.createPersistentProperty("ExtendPower", 0.1);
        retractPower = pf.createPersistentProperty("RetractPower", 0.1);

        setPowerMax = pf.createPersistentProperty("SetPowerMax", 0.5);
        setPowerMin = pf.createPersistentProperty("SetPowerMin", -0.5);

        armMotorLeft = sparkMaxFactory.createWithoutProperties(contract.getArmMotorLeft(), this.getPrefix(), "ArmMotorLeft");
        armMotorRight = sparkMaxFactory.createWithoutProperties(contract.getArmMotorRight(), this.getPrefix(), "ArmMotorRight");


        this.armState = ArmState.STOPPED;
    }

    public void setPower(double power) {

        // Put power within limit range (if not already)
        power = MathUtils.constrainDouble(power, setPowerMin.get(), setPowerMax.get());

        armMotorLeft.set(power);
        armMotorRight.set(power);
    }

    public void extend() {
        setPower(extendPower.get());
        armState = ArmState.EXTENDING;
    }

    public void retract() {
        setPower(retractPower.get());
        armState = ArmState.RETRACTING;
    }

    public void stop() {
        setPower(0);
        armState = ArmState.STOPPED;
    }
}
