package competition.auto;

import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class PodiumMidCommandGroup extends SequentialCommandGroup {

    @Inject
    public PodiumMidCommandGroup(PoseSubsystem pose,
                                 FireFromSubwooferCommandGroup firePreload,
                                 PodiumMidCommand podiumMidCommand,
                                 ContinuouslyWarmUpForSpeakerCommand continuouslyWarmUpForSpeakerCommand) {

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        this.addCommands(firePreload);

        this.addCommands(Commands.parallel(podiumMidCommand, continuouslyWarmUpForSpeakerCommand));
    }
}
