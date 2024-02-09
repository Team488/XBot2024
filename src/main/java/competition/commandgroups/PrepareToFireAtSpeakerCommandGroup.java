package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterDistanceToRpmConverter;
import competition.subsystems.shooter.commands.WarmUpShooterRPMCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToFireAtSpeakerCommandGroup extends ParallelCommandGroup {

    @Inject
    public PrepareToFireAtSpeakerCommandGroup(SetArmAngleCommand armAngle,
                                              WarmUpShooterRPMCommand shooter,
                                              PoseSubsystem pose,
                                              ArmSubsystem arm) {
        // Move arm to preset position
        armAngle.setTargetAngle(arm.getArmAngleFromDistance(pose.getDistanceFromSpeaker()));
        this.addCommands(armAngle);
        // Set shooter wheels to target RPM
        ShooterDistanceToRpmConverter distToRpm = new ShooterDistanceToRpmConverter();
        shooter.setTargetRpm(distToRpm.getRPMForDistance(pose.getDistanceFromSpeaker()));
        this.addCommands(shooter);
    }
}
