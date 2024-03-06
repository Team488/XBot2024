package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class FireFromTopBottomSpikeCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public FireFromTopBottomSpikeCommandGroup(WarmUpShooterCommand warmUpShooterCommand,
                                              FireWhenReadyCommand fireWhenReadyCommand,
                                              SetArmExtensionCommand setArmExtensionCommand) {
        super(fireWhenReadyCommand);

        warmUpShooterCommand.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        setArmExtensionCommand.setTargetExtension(ArmSubsystem.UsefulArmPosition.PROTECTED_PODIUM_SHOT);

        this.addCommands(setArmExtensionCommand, warmUpShooterCommand);
    }
}
