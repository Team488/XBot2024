package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class SetArmAndShooterForAmpCommandGroup extends ParallelCommandGroup {

    @Inject
    public SetArmAndShooterForAmpCommandGroup(SetArmAngleCommand armAngle,
                                              WarmUpShooterCommand shooter) {
        // Move arm to preset position
        armAngle.setArmPosition(ArmSubsystem.UsefulArmPosition.FIRING_IN_AMP);
        this.addCommands(armAngle);
        // Set shooter wheels to target RPM
        shooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.AMP_SHOT);
        this.addCommands(shooter);
    }
}
