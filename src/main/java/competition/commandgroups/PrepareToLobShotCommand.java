package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class PrepareToLobShotCommand extends BaseSetpointCommand {
    SetArmExtensionCommand setArmExtension;
    WarmUpShooterCommand warmUpShooter;


    @Inject
    public PrepareToLobShotCommand(SetArmExtensionCommand setArmExtension, WarmUpShooterCommand warmUpShooter,
                                   ShooterWheelSubsystem shooter) {
        super(shooter);
        this.setArmExtension = setArmExtension;
        this.warmUpShooter = warmUpShooter;
    }

    @Override
    public void initialize() {
        warmUpShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.LOB_SHOT);
        setArmExtension.setTargetExtension(ArmSubsystem.UsefulArmPosition.LOB_SHOT);
    }

    @Override
    public void execute() {
        // Empty
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
