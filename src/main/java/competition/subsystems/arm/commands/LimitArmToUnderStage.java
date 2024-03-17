package competition.subsystems.arm.commands;

import javax.inject.Inject;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

public class LimitArmToUnderStage extends BaseCommand {

    final ArmSubsystem arm;

    @Inject
    public LimitArmToUnderStage(ArmSubsystem arm) {
        this.arm = arm;
    }

    @Override
    public void initialize() {
        this.arm.setLimitToUnderStage(true);
    }
    
    @Override
    public void end(boolean interrupted) {
        this.arm.setLimitToUnderStage(false);
    }
}
