package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class RemoveForcedBrakingCommand extends BaseCommand {

    ArmSubsystem arm;

    @Inject
    public RemoveForcedBrakingCommand(ArmSubsystem arm) {
        this.arm = arm;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setForceBrakesEngaged(false);
    }
}
