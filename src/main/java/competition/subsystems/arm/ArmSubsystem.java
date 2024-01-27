package competition.subsystems.arm;

import com.revrobotics.SparkLimitSwitch;
import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import org.littletonrobotics.junction.Logger;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.math.MathUtils;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ArmSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    public final XCANSparkMax armMotorLeft;
    public final XCANSparkMax armMotorRight;

    public ArmState armState;

    public DoubleProperty extendPower;
    public DoubleProperty retractPower;

    private DoubleProperty armPowerMax;
    private DoubleProperty armPowerMin;
  
    public DoubleProperty ticksToMmRatio; // Millimeters
    public DoubleProperty armMotorRevolutionOffset; // # of revolutions
    public DoubleProperty armMotorRevolutionLimit;
    boolean hasSetTruePositionOffset;

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

        // All the DoubleProperties below needs configuration.
        ticksToMmRatio = pf.createPersistentProperty("TicksToDistanceRatio", 1000);
        armMotorRevolutionOffset = pf.createPersistentProperty("ArmMotorLeftPositionOffset", 0);
        armMotorRevolutionLimit = pf.createPersistentProperty("ArmMotorPositionLimit", 15000);
        hasSetTruePositionOffset = false;

        armMotorLeft = sparkMaxFactory.createWithoutProperties(
                contract.getArmMotorLeft(), this.getPrefix(), "ArmMotorLeft");
        armMotorRight = sparkMaxFactory.createWithoutProperties(
                contract.getArmMotorRight(), this.getPrefix(), "ArmMotorRight");

        this.armState = ArmState.STOPPED;
    }

    public void setPower(double leftPower, double rightPower) {

        // Check if armPowerMin/armPowerMax are safe values
        if (armPowerMax.get() < 0 || armPowerMin.get() > 0) {
            armMotorLeft.set(0);
            armMotorRight.set(0);
            log.error("armPowerMax or armPowerMin values out of bound!");
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

    public double ticksToDistance(double ticks) {
        return ticksToMmRatio.get() * ticks;
    }

    public double ticksToShooterAngle(double ticks) {
        return ticks * 1000; // To be modified into ticks to shooter angle formula
    }

    public void armEncoderTicksUpdate() {
        Logger.recordOutput(getPrefix() + "ArmMotorLeftTicks", armMotorLeft.getPosition());
        Logger.recordOutput(getPrefix() + "ArmMotorRightTicks", armMotorRight.getPosition());
        Logger.recordOutput(getPrefix() + "ArmMotorLeftDistance", ticksToDistance(
                armMotorLeft.getPosition() + armMotorRevolutionOffset.get()));
        Logger.recordOutput(getPrefix() + "ArmMotorRightDistance", ticksToDistance(
                armMotorRight.getPosition() + armMotorRevolutionOffset.get()));

        Logger.recordOutput(getPrefix() + "ArmMotorToShooterAngle", ticksToShooterAngle(
                (armMotorLeft.getPosition() + armMotorRight.getPosition() + armMotorRevolutionOffset.get()) / 2));
    }

    // Update the offset of the arm when it touches either forward/reverse limit switches for the first time.
    public void checkForArmOffset() {
        if (hasSetTruePositionOffset) {
            return;
        }

        // At max limit sensor?
        if (armMotorLeft.getForwardLimitSwitchPressed(SparkLimitSwitch.Type.kNormallyOpen)) {
            hasSetTruePositionOffset = true;
            armMotorRevolutionOffset.set(armMotorRevolutionLimit.get() - armMotorLeft.getPosition());
        } else if (armMotorLeft.getReverseLimitSwitchPressed(SparkLimitSwitch.Type.kNormallyOpen)) {
            // At min (lowest) limit sensor?
            hasSetTruePositionOffset = true;
            armMotorRevolutionOffset.set(-armMotorLeft.getPosition());
        }
    }

    public void periodic() {
        armEncoderTicksUpdate();
        checkForArmOffset();
        Logger.recordOutput(getPrefix() + "Arm3dState", new Pose3d(new Translation3d(0, 0, 0), new Rotation3d(0, 0, 0)));
    }

    @Override
    public void refreshDataFrame() {
        armMotorLeft.refreshDataFrame();
        armMotorRight.refreshDataFrame();
    }
}