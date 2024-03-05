package competition.auto_pathplanner;

import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class Far4NoteCommandGroup extends SequentialCommandGroup {

    @Inject
    public Far4NoteCommandGroup(PoseSubsystem pose,
                                FireFromSubwooferCommandGroup firePreload,
                                Fast4NoteFarCommand fast4NoteFarCommand,
                                ContinuouslyWarmUpForSpeakerCommand continuouslyWarmUpForSpeakerCommand) {

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        this.addCommands(firePreload);

        this.addCommands(Commands.parallel(fast4NoteFarCommand, continuouslyWarmUpForSpeakerCommand));
    }
}
