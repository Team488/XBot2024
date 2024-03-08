package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class ShooterWheelMaintainerCommand extends BaseMaintainerCommand<ShooterWheelTargetSpeeds> {

    final ShooterWheelSubsystem wheel;
    final OperatorInterface oi;
    final ArmSubsystem arm;

    final DoubleProperty subwooferRpmErrorTolerance;
    final DoubleProperty ampRpmErrorTolerance;

    @Inject
    public ShooterWheelMaintainerCommand(OperatorInterface oi, ShooterWheelSubsystem wheel, PropertyFactory pf,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory, ArmSubsystem arm) {
        super(wheel, pf, hvmFactory, 150, 0.1);
        this.oi = oi;
        this.wheel = wheel;
        this.arm = arm;

        subwooferRpmErrorTolerance = pf.createPersistentProperty("Subwoofer RPM Error Tolerance", 1000);
        ampRpmErrorTolerance = pf.createPersistentProperty("Amp RPM Error Tolerance", 1000);
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
        wheel.configurePID();
    }

    @Override
    protected void coastAction() {
        // DOES NOT NEED TO HAVE ANYTHING
    }

    protected void initializeMachineControlAction() {
        wheel.resetPID();
        super.initializeMachineControlAction();
    }

    @Override
    protected void calibratedMachineControlAction() {
        var speeds = wheel.getTargetValue();


        if (speeds.representsZeroSpeed()) {
            wheel.setPower(new ShooterWheelTargetSpeeds(0.0));
            return;
        }

        if (wheel.isCalibrated()) {
            wheel.setPidSetpoints(speeds);
        }
    }

    @Override
    protected ShooterWheelTargetSpeeds getHumanInput() {
        // I THINK WE CURRENTLY DON'T HAVE ANY HUMAN INPUT.
        return new ShooterWheelTargetSpeeds(0,0);
    }

    protected void end() {
        wheel.resetWheel();
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0.0;
    }

    @Override
    protected double getErrorMagnitude() {
        var currents = wheel.getCurrentValue();
        var targets = wheel.getTargetValue();

        // Take the average of the two errors for now.
        return (Math.abs(targets.upperWheelsTargetRPM - currents.upperWheelsTargetRPM)
                + Math.abs(targets.lowerWheelsTargetRPM - currents.lowerWheelsTargetRPM)) / 2;
    }

    @Override
    public boolean getErrorWithinTolerance() {
        // If the arm is up really high, we are scoring in the amp and can have a big error tolerance.

        if (arm.getExtensionDistance() > 125) {
            return Math.abs(getErrorMagnitude()) < ampRpmErrorTolerance.get();
        }

        // If the arm is at its minimum, we are scoring from Subwoofer and can have a large-ish error tolerance.
        if (arm.getExtensionDistance() < 10) {
            return Math.abs(getErrorMagnitude()) < subwooferRpmErrorTolerance.get();
        }

        // Other shots likely require more precision.
        return super.getErrorWithinTolerance();
    }
}
