package competition.auto;

import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.commandgroups.PrepareToFireAtSpeakerCommandGroup;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class MidNoteCommandGroup extends SequentialCommandGroup {
    @Inject
    public MidNoteCommandGroup(MidNoteCommand midNoteCommand,
                               Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                               Provider<PrepareEverywhereCommandGroup> prepareEverywhereCommandGroupProvider,
                               Provider<WarmUpShooterCommand> warmUpShooterCommandProvider, PoseSubsystem pose,
                               Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider) {

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SubwooferCentralScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        var firePreloadNote = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(firePreloadNote);

        this.addCommands(midNoteCommand);

    }
}
