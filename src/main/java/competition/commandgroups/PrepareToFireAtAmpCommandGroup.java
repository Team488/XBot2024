package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToFireAtAmpCommandGroup extends ParallelCommandGroup {

    @Inject
    public PrepareToFireAtAmpCommandGroup(SetArmExtensionCommand setArmExtensionCommand,
                                          WarmUpShooterCommand shooter) {
        // Move arm to preset position
        setArmExtensionCommand.setTargetExtension(ArmSubsystem.UsefulArmPosition.FIRING_FROM_AMP);
        this.addCommands(setArmExtensionCommand);
        // Set shooter wheels to target RPM
        shooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);
        this.addCommands(shooter);
    }
}
