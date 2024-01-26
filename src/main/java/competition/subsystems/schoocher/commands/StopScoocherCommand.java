package competition.subsystems.schoocher.commands;

import javax.inject.Inject;

import competition.subsystems.schoocher.ScoocherSubsystem;
import xbot.common.command.BaseCommand;

public class StopScoocherCommand extends BaseCommand{
    ScoocherSubsystem scoocher;
    public StopScoocherCommand(ScoocherSubsystem scoocher){
        this.scoocher = scoocher;
        addRequirements(scoocher);
    }
    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        scoocher.stop();
    }
}
