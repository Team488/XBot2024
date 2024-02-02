package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

public class ShooterWheelMaintainerCommand extends BaseMaintainerCommand<Double> {

    final ShooterWheelSubsystem wheel;
    final OperatorInterface oi;

    public ShooterWheelMaintainerCommand(OperatorInterface oi, ShooterWheelSubsystem wheel, PropertyFactory pf,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory) {
        super(wheel, pf, hvmFactory, 0, 0);
        this.oi = oi;
        this.wheel = wheel;
        this.addRequirements(this.wheel);
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
        double speed = wheel.getTargetValue();

        if (wheel.isCalibrated()) {
            wheel.setPower(1.0);
        }
    }

    @Override
    protected Double getHumanInput() {
        // I THINK WE CURRENTLY DON'T HAVE ANY HUMAN INPUT.
        return 0.0;
    }

    protected void end() {
        wheel.resetWheel();
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0.0;
    }

    protected boolean getErrorWithinTolerance() {
        // THESE VALUES NEED TUNING
        double tolerance = 400;
        double limit = 1000;

        if (wheel.getTargetValue() < limit) {
            tolerance = wheel.getShortRangeErrorTolerance();
        } else {
            tolerance = wheel.getLongRangeErrorTolerance();
        }
        return false;
    }

    @Override
    protected double getErrorMagnitude() {
        return 0;
    }
}
