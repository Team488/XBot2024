package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ManipulateArmBrakeCommand extends BaseCommand {

    ArmSubsystem arm;
    boolean brakeEngaged;

    @Inject
    public ManipulateArmBrakeCommand(ArmSubsystem arm) {
        this.arm = arm;
        addRequirements(arm);
    }

    public void setBrakeMode(boolean brakeEngaged) {
        this.brakeEngaged = brakeEngaged;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        log.info("Setting arm brake to " + brakeEngaged);
        arm.setBrakeState(brakeEngaged);
    }

    @Override
    public void execute() {
        // No-op.
    }
}
