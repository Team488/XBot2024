package competition.subsystems.arm.commands;

import javax.inject.Inject;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

public class CalibrateArmsManuallyCommand extends BaseCommand {
    public final ArmSubsystem armSubsystem;

    @Inject
    public CalibrateArmsManuallyCommand(ArmSubsystem armSubsystem) {
        this.addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;

    }

    @Override
    public void initialize() {
        armSubsystem.calibrateArmsHere();
    }

    @Override
    public void execute() {
        
    }

    @Override
    public boolean isFinished() {
        return true;
    }   
    
}
