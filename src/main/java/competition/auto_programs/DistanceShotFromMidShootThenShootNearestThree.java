package competition.auto_programs;

import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.PointAtSpeakerCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class DistanceShotFromMidShootThenShootNearestThree extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public DistanceShotFromMidShootThenShootNearestThree(AutonomousCommandSelector autoSelector,
                                                         Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                                         Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                                                         Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                                         PoseSubsystem pose, DriveSubsystem drive,
                                                         Provider<PointAtSpeakerCommand> pointAtSpeakerCommandProvider,
                                                         FireFromSubwooferCommandGroup fireFromSubwooferCommandGroup) {
        this.autoSelector = autoSelector;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferCentralScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

//        // Fire note into the speaker from starting position
//        var fireFirstNoteCommand = fireNoteCommandGroupProvider.get();
//        this.addCommands(Commands.deadline(fireFirstNoteCommand));
        // Fire preload note into the speaker from starting position
        this.addCommands(Commands.deadline(fireFromSubwooferCommandGroup));

        // Drive to top spike note and collect
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.SpikeTop);
                })
        );
        var driveToMiddleSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(Commands.deadline(driveToMiddleSpikeNoteAndCollect));

        // Point at speaker
        var pointAtSpeakerFirst = pointAtSpeakerCommandProvider.get();

        // Fire Note into the speaker
        var fireSecondNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireSecondNoteCommand, pointAtSpeakerFirst));

        // Drive to top spike note and collect
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.SpikeMiddle);
                })
        );
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(Commands.deadline(driveToTopSpikeNoteAndCollect));

        // Point at speaker
        var pointAtSpeakerSecond = pointAtSpeakerCommandProvider.get();

        // Fire Note into the speaker
        var fireThirdNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireThirdNoteCommand, pointAtSpeakerSecond));

        // Drive to bottom spike note and collect
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.SpikeBottom);
                })
        );
        var driveToBottomSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToBottomSpikeNoteAndCollect);

        // Point at speaker
        var pointAtSpeakerThird = pointAtSpeakerCommandProvider.get();

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireFourthNoteCommand, pointAtSpeakerThird));
    }

}