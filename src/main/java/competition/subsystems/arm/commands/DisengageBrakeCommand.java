package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;

import javax.inject.Inject;

public class DisengageBrakeCommand extends ManipulateArmBrakeCommand {

    @Inject
    public DisengageBrakeCommand(ArmSubsystem arm) {
        super(arm);
        this.setBrakeMode(false);
    }
}
