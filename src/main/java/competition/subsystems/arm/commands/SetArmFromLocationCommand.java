package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class SetArmFromLocationCommand extends BaseSetpointCommand {

    ArmSubsystem arm;

    @Inject
    SetArmFromLocationCommand(ArmSubsystem arm) {
        super(arm);
        this.arm = arm;
    }

    @Override
    public void initialize() {
        arm.setTargetValue(arm.getAngleFromRange());
    }

    @Override
    public void execute() {
        // Nothing to do here
    }
}
