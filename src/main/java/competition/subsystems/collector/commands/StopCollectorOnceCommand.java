package competition.subsystems.collector.commands;

import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class StopCollectorOnceCommand extends BaseCommand {
    CollectorSubsystem collector;

    @Inject
    public StopCollectorOnceCommand(CollectorSubsystem collector) {
        this.collector = collector;
        addRequirements(collector);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.stop();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}