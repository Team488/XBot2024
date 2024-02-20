package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class SetArmTargetToCurrentPositionCommand extends BaseSetpointCommand {

    ArmSubsystem arm;

    @Inject
    public SetArmTargetToCurrentPositionCommand(ArmSubsystem arm) {
        super(arm);
        this.arm = arm;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setTargetValue(arm.getCurrentValue());
    }

    @Override
    public void execute() {
        // No-op.
    }
}
