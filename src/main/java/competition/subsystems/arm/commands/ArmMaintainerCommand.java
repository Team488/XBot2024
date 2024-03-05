package competition.subsystems.arm.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.logic.CalibrationDecider;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.logic.TimeStableValidator;
import xbot.common.math.MathUtils;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
public class ArmMaintainerCommand extends BaseMaintainerCommand<Double> {
    private final ArmSubsystem arm;
    private final PIDManager positionPid;
    // oi used for human input
    private final OperatorInterface oi;

    boolean startedCalibration = false;
    boolean givenUpOnCalibration = false;
    double calibrationStartTime = 0;
    double calibrationMaxDuration = 5;
    TimeStableValidator calibrationValidator;
    double timeToBrake;
    final double calibrationStallDurationSec = 0.5;

    @Inject
    public ArmMaintainerCommand(ArmSubsystem arm, PropertyFactory pf,
                                HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                                PIDManager.PIDManagerFactory pidf,
                                CalibrationDecider.CalibrationDeciderFactory calf, OperatorInterface oi){
        super(arm, pf, hvmFactory, 1, 0.5);
        this.arm = arm;
        this.oi = oi;
        pf.setPrefix(this);
        positionPid = pidf.create(getPrefix() + "PoisitionPID", 0.0125, 0.0, 0);

        calibrationValidator = new TimeStableValidator(() -> calibrationStallDurationSec);
    }
    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setTargetValue(arm.getCurrentValue());
    }

    @Override
    protected void coastAction() {
        arm.setPower(0.0);
    }

    @Override
    protected void calibratedMachineControlAction() {

        // The arms can draw huge currents when trying to move small values, so if we are on target
        // then we need to kill power.
        if (isMaintainerAtGoal() || shouldFreezeArmSinceEndOfMatch()) {
            arm.setPower(0.0);
        } else {
            double power = positionPid.calculate(arm.getTargetValue(), arm.getCurrentValue());
            arm.setPower(power);
        }
    }

    private boolean shouldFreezeArmSinceEndOfMatch() {
        return DriverStation.getMatchTime() < 1 && arm.couldPlausiblyBeHanging();
    }

    @Override
    protected void humanControlAction() {
        if (shouldFreezeArmSinceEndOfMatch()) {
            arm.setPower(0.0);
        } else {
            super.humanControlAction();
        }
    }

    @Override
    protected void uncalibratedMachineControlAction() {
        aKitLog.record("Started Calibration", startedCalibration);
        aKitLog.record("Given Up On Calibration", givenUpOnCalibration);

        // Try to auto-calibrate.
        if (!startedCalibration) {
            calibrationStartTime = XTimer.getFPGATimestamp();
            startedCalibration = true;
        }

        if (calibrationStartTime + calibrationMaxDuration < XTimer.getFPGATimestamp()) {
            givenUpOnCalibration = true;
        }

        if (!givenUpOnCalibration) {
            // Set some tiny small power to get the arm moving down
            arm.setPower(arm.lowerExtremelySlowZonePowerLimit.get());

            // Are we above 5A usage?
            boolean stalledCurrent = arm.armMotorLeft.getOutputCurrent() > 5
                    && arm.armMotorRight.getOutputCurrent() > 5;

            aKitLog.record("StalledCurrent", stalledCurrent);

            // Are the arms still?
            boolean stillArms = arm.armMotorLeft.getVelocity() < 0.1
                    && arm.armMotorRight.getVelocity() < 0.1;

            aKitLog.record("StillArms", stillArms);

            boolean stableAtBottom = calibrationValidator.checkStable(stalledCurrent && stillArms);

            if (stableAtBottom) {
                arm.markArmsAsCalibratedAgainstLowerPhyscalLimit();
                // If nobody is currently commanding a setpoint, this will clear the setpoint
                // so the arms don't move from the 0 position they just calibrated to.
                // If there is an active setpoint, it will override this quite quickly (example; autonomous trying
                // to move the arm to a position but has to wait for calibration).
                arm.setTargetValue(arm.getCurrentValue());
            }
        } else {
            humanControlAction();
        }
    }

    @Override
    protected double getErrorMagnitude() {
            double current = arm.getCurrentValue();
            double target = arm.getTargetValue();
            double armError = Math.abs(target - current);
            return armError;
    }

    @Override
    protected Double getHumanInput() {
        double fundamentalInput = MathUtils.deadband(
                oi.operatorFundamentalsGamepad.getLeftVector().y,
                oi.getOperatorGamepadTypicalDeadband(),
                (x) -> x);

        double advancedInput = MathUtils.deadband(
                oi.operatorGamepadAdvanced.getLeftVector().y,
                oi.getOperatorGamepadTypicalDeadband(),
                (x) -> x);

        return fundamentalInput + advancedInput;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return Math.abs(getHumanInput());
    }
}
