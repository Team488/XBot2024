package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterDistanceToRpmConverter;
import competition.subsystems.shooter.commands.WarmUpShooterRPMCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class SetArmAndShooterForSpeakerCommandGroup extends ParallelCommandGroup {

    @Inject
    public SetArmAndShooterForSpeakerCommandGroup(SetArmAngleCommand armAngle,
                                                  WarmUpShooterRPMCommand shooter,
                                                  ShooterDistanceToRpmConverter distToRpm,
                                                  PoseSubsystem pose,
                                                  ArmSubsystem arm) {
        // Move arm to preset position
        armAngle.setTargetAngle(arm.getArmAngleFromDistance(pose.getDistanceFromSpeaker()));
        this.addCommands(armAngle);
        // Set shooter wheels to target RPM
        shooter.setTargetRpm(distToRpm.getRPMForDistance(pose.getDistanceFromSpeaker()));
        this.addCommands(shooter);
    }
}
