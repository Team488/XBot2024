package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class SetArmAndShooterCommandGroup extends ParallelCommandGroup {

    @Inject
    public SetArmAndShooterCommandGroup(SetArmAngleCommand armAngle,
                                        WarmUpShooterCommand shooter,
                                        ArmSubsystem.UsefulArmPosition armPosition,
                                        ShooterWheelSubsystem.TargetRPM targetRpm) {
        // Move arm to preset position
        armAngle.setArmPosition(armPosition);
        this.addCommands(armAngle);
        // Set shooter wheels to target RPM
        shooter.setTargetRpm(targetRpm);
        this.addCommands(shooter);
    }
}
