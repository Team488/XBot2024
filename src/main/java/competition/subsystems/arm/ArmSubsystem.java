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

    private DoubleProperty armPowerMax;
    private DoubleProperty armPowerMin;

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
      
        armPowerMax = pf.createPersistentProperty("ArmPowerMax", 0.5);
        armPowerMin = pf.createPersistentProperty("ArmPowerMin", -0.5);

        armMotorLeft = sparkMaxFactory.createWithoutProperties(contract.getArmMotorLeft(), this.getPrefix(), "ArmMotorLeft");
        armMotorRight = sparkMaxFactory.createWithoutProperties(contract.getArmMotorRight(), this.getPrefix(), "ArmMotorRight");


        this.armState = ArmState.STOPPED;
    }

    public void setPower(double leftPower, double rightPower) {

        // Check if armPowerMin/armPowerMax are safe values
        if (armPowerMax.get() < 0 || armPowerMin.get() > 0) {
            armMotorLeft.set(0);
            armMotorRight.set(0);
            System.out.println("armPowerMax or armPowerMin values out of bound!");
            return;
        }

        // Put power within limit range (if not already)
        leftPower = MathUtils.constrainDouble(leftPower, armPowerMin.get(), armPowerMax.get());
        rightPower = MathUtils.constrainDouble(rightPower, armPowerMin.get(), armPowerMax.get());

        armMotorLeft.set(leftPower);
        armMotorRight.set(rightPower);
    }

    /**
     * This sets one power to both left and right arms at the same time
     * @param power the power to send to both arms
     */
    public void setPower(double power) {
        setPower(power, power);
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
