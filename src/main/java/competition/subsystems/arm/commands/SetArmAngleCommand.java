package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class SetArmAngleCommand extends BaseSetpointCommand {

    ArmSubsystem armSubsystem;

    private double targetAngle;

    @Inject
    public SetArmAngleCommand(ArmSubsystem armSubsystem) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    public void setTargetAngle(double targetAngle) {
        this.targetAngle = targetAngle;
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
