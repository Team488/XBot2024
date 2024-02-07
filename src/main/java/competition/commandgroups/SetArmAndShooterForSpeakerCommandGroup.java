package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class SetArmAndShooterForSpeakerCommandGroup extends ParallelCommandGroup {

    @Inject
    public SetArmAndShooterForSpeakerCommandGroup(SetArmAngleCommand armAngle,
                                                  WarmUpShooterCommand shooter,
                                                  PoseSubsystem pose,
                                                  ArmSubsystem arm) {
        armAngle.setTargetAngle(arm.getArmAngleFromDistance(pose.getDistanceFromSpeaker()));
        // Move arm to preset position
        this.addCommands(armAngle);
        // Set shooter wheels to target RPM
        this.addCommands(shooter);
    }
}
