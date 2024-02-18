package competition.subsystems.arm.commands;

import javax.inject.Inject;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

public class SetArmExtensionCommand extends BaseSetpointCommand {

    private double targetExtension;
    private final ArmSubsystem armSubsystem;
    private boolean isRelative = false;

    @Inject
    public SetArmExtensionCommand(ArmSubsystem armSubsystem) {
        this.armSubsystem = armSubsystem;
    }

    public void setTargetExtension(double targetExtension) {
        this.targetExtension = targetExtension;
    }

    public void setRelative(boolean isRelative) {
        this.isRelative = isRelative;
    }

    @Override
    public void initialize() {
        if(isRelative) {
            armSubsystem.setTargetValue(armSubsystem.getCurrentValue() + targetExtension);
        } else {
            armSubsystem.setTargetValue(targetExtension);
        }
    }

    @Override
    public void execute() {
        // no-op
    }
    
}
