package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class SetArmAngleCommand extends BaseSetpointCommand {

    ArmSubsystem armSubsystem;

    private double targetAngle;

    @Inject
    public SetArmAngleCommand(ArmSubsystem armSubsystem) {
        this.armSubsystem = armSubsystem;
    }

    public void setTargetAngle(double targetAngle) {
        this.targetAngle = targetAngle;
    }

    public void setArmPosition(ArmSubsystem.UsefulArmPosition armPosition) {
        this.targetAngle = armSubsystem.getUsefulArmPosition(armPosition);
    }

    @Override
    public void initialize() {
        armSubsystem.setTargetValue(targetAngle);
    }

    @Override
    public void execute() {
        // No-op. Set angle for maintainer to move arms to.
    }
}
