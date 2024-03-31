package competition.subsystems.collector.commands;

import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;
public class FireCollectorCommand extends BaseCommand {
    CollectorSubsystem collector;
    @Inject
    public FireCollectorCommand(CollectorSubsystem collector){
        addRequirements(collector);
        this.collector = collector;
    }
    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.fire();
    }

    @Override
    public void end(boolean interrupted) {
        collector.stop();
    }
}
