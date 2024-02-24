package competition.commandgroups;

import competition.subsystems.arm.commands.ContinuouslyPointArmAtSpeakerCommand;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToFireAtSpeakerCommandGroup extends ParallelCommandGroup {

    @Inject
    public PrepareToFireAtSpeakerCommandGroup(ContinuouslyWarmUpForSpeakerCommand warmUpShooter,
                                              ContinuouslyPointArmAtSpeakerCommand warmUpArm) {
        // Move arm to preset position
        this.addCommands(warmUpShooter, warmUpArm);
    }
}
