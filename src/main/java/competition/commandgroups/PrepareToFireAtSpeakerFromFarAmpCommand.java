package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToFireAtSpeakerFromFarAmpCommand  extends ParallelCommandGroup {

    @Inject
    public PrepareToFireAtSpeakerFromFarAmpCommand(
            WarmUpShooterCommand warmUpShooter,
            SetArmExtensionCommand setArmExtension,
            ArmSubsystem arm) {
        warmUpShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        setArmExtension.setTargetExtension(ArmSubsystem.UsefulArmPosition.PROTECTED_FAR_AMP_SHOT);

        this.addCommands(warmUpShooter, setArmExtension);
    }
}

