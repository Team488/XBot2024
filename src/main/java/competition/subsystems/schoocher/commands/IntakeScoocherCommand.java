package competition.subsystems.schoocher.commands;

import javax.inject.Inject;

import competition.subsystems.schoocher.ScoocherSubsystem;
import xbot.common.command.BaseCommand;

public class IntakeScoocherCommand extends BaseCommand {
    ScoocherSubsystem scoocher;
    @Inject
    public IntakeScoocherCommand(ScoocherSubsystem scoocher){
        this.scoocher = scoocher;
        addRequirements(scoocher);
    }
    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        scoocher.intakeNote();
    }
}
