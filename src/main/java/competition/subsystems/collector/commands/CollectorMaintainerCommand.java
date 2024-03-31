package competition.subsystems.collector.commands;

import javax.inject.Inject;

import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.logic.HumanVsMachineDecider.HumanVsMachineDeciderFactory;
import xbot.common.properties.PropertyFactory;

public class CollectorMaintainerCommand extends BaseMaintainerCommand<Double> {

    final CollectorSubsystem collector;

    @Inject
    public CollectorMaintainerCommand(CollectorSubsystem collector, PropertyFactory pf,
            HumanVsMachineDeciderFactory humanVsMachineDeciderFactory, double defaultErrorTolerance,
            double defaultTimeStableWindow) {
        super(collector, pf, humanVsMachineDeciderFactory, defaultErrorTolerance, defaultTimeStableWindow);
        this.collector = collector;
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
    }
    
    @Override
    protected void coastAction() {
        // no action
    }

    @Override
    protected void calibratedMachineControlAction() {
        collector.setPidSetpoints(collector.getTargetValue());
    }

    @Override
    protected double getErrorMagnitude() {
        return Math.abs(this.collector.getTargetValue() - this.collector.getCurrentValue());
    }

    @Override
    protected Double getHumanInput() {
        return 0.0; // no human input
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0.0; // no human input
    }

    
}
