package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;

import javax.inject.Inject;

public class EngageBrakeCommand extends ManipulateArmBrakeCommand {

    @Inject
    public EngageBrakeCommand(ArmSubsystem arm) {
        super(arm);
        this.setBrakeMode(true);
    }
}
