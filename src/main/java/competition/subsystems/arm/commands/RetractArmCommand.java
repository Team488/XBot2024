package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class RetractArmCommand extends BaseCommand {

    ArmSubsystem armSubsystem;

    @Inject
    public RetractArmCommand(ArmSubsystem armSubsystem) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    @Override
    public void initialize() {
        log.info("RetractArmCommand initializing");
    }

    @Override
    public void execute() {
        armSubsystem.retract();
    }
}
