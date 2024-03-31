package competition.subsystems.collector.commands;

import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;


public class StopCollectorCommand extends BaseSetpointCommand {
    CollectorSubsystem collector;

    @Inject
    public StopCollectorCommand(CollectorSubsystem collector) {
        super(collector);
        this.collector = collector;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.stop();
    }
}