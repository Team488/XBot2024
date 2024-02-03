package competition.subsystems.collector.commands;

import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeUntilNoteCollectedCommand extends BaseCommand {
    CollectorSubsystem collector;
    @Inject
    public IntakeUntilNoteCollectedCommand(CollectorSubsystem collector) {
        this.collector = collector;
        addRequirements(collector);
    }
    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.intake();
    }
    @Override
    public boolean isFinished() {
        return collector.getGamePieceReady();
    }
}
