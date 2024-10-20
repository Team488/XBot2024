package competition.subsystems.collector.commands;

import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class StopCollectorCommand extends BaseCommand {
    CollectorSubsystem collector;

    @Inject
    public StopCollectorCommand(CollectorSubsystem collector) {
        addRequirements(collector);
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