package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

public class DefaultToCurrentPositionCommand extends BaseSetpointCommand {

    ArmSubsystem arm;

    public DefaultToCurrentPositionCommand(ArmSubsystem arm) {
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
