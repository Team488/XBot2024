package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HangCommand extends BaseCommand {
    ArmSubsystem armSubsystem;

    @Inject
    public HangCommand(ArmSubsystem armSubsystem) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    @Override
    public void initialize() {
        log.info("HangCommand initializing");
    }

    @Override
    public void execute() {
        armSubsystem.hang();
    }
}
