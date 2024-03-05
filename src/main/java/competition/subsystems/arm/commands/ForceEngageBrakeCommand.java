package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;
import javax.naming.InitialContext;

public class ForceEngageBrakeCommand extends BaseCommand {

    ArmSubsystem arm;

    @Inject
    public ForceEngageBrakeCommand(ArmSubsystem arm) {
        this.arm = arm;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setForceBrakesEngaged(true);
    }
}
