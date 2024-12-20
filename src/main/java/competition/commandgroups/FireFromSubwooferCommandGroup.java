package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class FireFromSubwooferCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public FireFromSubwooferCommandGroup(WarmUpShooterCommand warmUpShooterCommand,
                                         FireWhenReadyCommand fireWhenReadyCommand,
                                         SetArmExtensionCommand setArmExtensionCommand) {
        super(fireWhenReadyCommand);

        warmUpShooterCommand.setTargetRpm(ShooterWheelSubsystem.TargetRPM.MELEE);
        setArmExtensionCommand.setTargetExtension(ArmSubsystem.UsefulArmPosition.FIRING_FROM_SUBWOOFER);

        this.addCommands(setArmExtensionCommand, warmUpShooterCommand);
    }
}
