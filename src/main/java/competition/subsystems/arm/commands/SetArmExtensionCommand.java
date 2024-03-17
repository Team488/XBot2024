package competition.subsystems.arm.commands;

import javax.inject.Inject;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

public class SetArmExtensionCommand extends BaseSetpointCommand {

    private double targetExtension;
    private boolean isRelative = false;
    
    private final ArmSubsystem armSubsystem;

    @Inject
    public SetArmExtensionCommand(ArmSubsystem armSubsystem) {
        super(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    public void setTargetExtension(double targetExtension) {
        this.targetExtension = targetExtension;
    }

    public void setTargetExtension(ArmSubsystem.UsefulArmPosition position) {
        setTargetExtension(armSubsystem.getUsefulArmPositionExtensionInMm(position));
    }

    public void setRelative(boolean isRelative) {
        this.isRelative = isRelative;
    }
    public double getArmExtensionForDistance(double distanceFromSpeaker){
        return armSubsystem.getRecommendedExtension(distanceFromSpeaker);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        double targetValue = targetExtension;
        if(isRelative) {
            targetValue = armSubsystem.getCurrentValue() + targetExtension;
        }
        log.info("Setting arm target extension to " + targetValue);
        armSubsystem.setTargetValue(targetValue);
        armSubsystem.initializeRampingPowerTarget();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
    
}
