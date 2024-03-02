package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ManualHangingModeCommand extends BaseCommand {

    ArmSubsystem arm;

    @Inject
    public ManualHangingModeCommand(ArmSubsystem arm) {
        this.arm = arm;
        this.addRequirements(arm);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setManualHangingMode(true);
    }

    @Override
    public void end(boolean interrupted) {
        arm.setManualHangingMode(false);
    }
}
