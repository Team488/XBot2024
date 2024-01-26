package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

public class ShooterWheelMaintainerCommand extends BaseMaintainerCommand<Double> {

    final ShooterWheelSubsystem wheel;
    final OperatorInterface oi;

    public ShooterWheelMaintainerCommand(OperatorInterface oi, ShooterWheelSubsystem wheel, PropertyFactory pf, HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory, ShooterWheelSubsystem wheel1) {
        super(wheel, pf, hvmFactory, 0, 0);
        this.oi = oi;
        this.wheel = wheel;
        this.addRequirements(this.wheel);
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
    }

    @Override
    protected void coastAction() {

    }

    @Override
    protected void calibratedMachineControlAction() {

    }

    @Override
    protected double getErrorMagnitude() {
        return 0;
    }

    @Override
    protected Double getHumanInput() {
        return 0.0;
    }

    

    @Override
    protected double getHumanInputMagnitude() {
        return 0;
    }
}
