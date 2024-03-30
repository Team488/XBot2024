package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToFireAtSpeakerFromUnderChainCommand extends ParallelCommandGroup {
    @Inject
    public PrepareToFireAtSpeakerFromUnderChainCommand(SetArmExtensionCommand setArmExtension,
                                                       WarmUpShooterCommand warmUpShooter,
                                                       ArmSubsystem arm) {

        warmUpShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.CHAIN_SHOT);
        setArmExtension.setTargetExtension(ArmSubsystem.UsefulArmPosition.CHAIN_SHOT);

        this.addCommands(warmUpShooter, setArmExtension);
    }
}
