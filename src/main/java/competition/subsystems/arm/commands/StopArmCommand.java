package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class StopArmCommand extends BaseCommand {

    ArmSubsystem armSubsystem;

    @Inject
    public StopArmCommand(ArmSubsystem armSubsystem) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    @Override
    public void initialize() {
        log.info("StopArmCommand initializing");
    }

    @Override
    public void execute() {
        armSubsystem.stop();
    }
}
