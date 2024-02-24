package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class SetArmAngleCommand extends BaseSetpointCommand {

    ArmSubsystem armSubsystem;
    ArmMaintainerCommand armMaintainerCommand;

    private double targetAngle;

    @Inject
    public SetArmAngleCommand(ArmSubsystem armSubsystem, ArmMaintainerCommand armMaintainerCommand) {
        this.armSubsystem = armSubsystem;
        this.armMaintainerCommand = armMaintainerCommand;
    }

    public void setTargetAngle(double targetAngle) {
        this.targetAngle = targetAngle;
    }

    public void setArmPosition(ArmSubsystem.UsefulArmPosition armPosition) {
        this.targetAngle = armSubsystem.getUsefulArmPositionAngle(armPosition);
    }

    @Override
    public void initialize() {
        armSubsystem.setTargetAngle(targetAngle);
    }

    @Override
    public void execute() {
        // No-op. Set angle for maintainer to move arms to.
    }

    @Override
    public boolean isFinished() {
        return true;
    }

}
