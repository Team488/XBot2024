package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.ArmSubsystem_Factory;
import xbot.common.command.BaseCommand;

public class WaitForArmToBeAtGoalCommand extends BaseCommand {
    ArmSubsystem arm;
    public WaitForArmToBeAtGoalCommand(ArmSubsystem arm){
        addRequirements(arm);
        this.arm = arm;
    }
    @Override
    public void initialize() {
        //nothing to do
    }

    @Override
    public void execute() {
        //nothing to do
    }

    @Override
    public boolean isFinished() {
        return arm.isMaintainerAtGoal();
    }
}
