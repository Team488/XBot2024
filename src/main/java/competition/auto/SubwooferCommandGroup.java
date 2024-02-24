package competition.auto;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class SubwooferCommandGroup extends ParallelCommandGroup {

    @Inject
    public SubwooferCommandGroup(SetArmAngleCommand armAngle,
                                          WarmUpShooterCommand shooter) {
        // Move arm to preset position
        armAngle.setArmPosition(ArmSubsystem.UsefulArmPosition.FIRING_FROM_SUBWOOFER);
        this.addCommands(armAngle);
        // Set shooter wheels to target RPM
        shooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        this.addCommands(shooter);
    }
}
