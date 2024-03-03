package competition.auto_programs;

import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import competition.subsystems.drive.commands.DriveToMidSpikeScoringLocationCommand;
import competition.subsystems.drive.commands.PointAtSpeakerCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class DistanceShotFromMidShootThenShootMiddleTopThenTopCenter extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public DistanceShotFromMidShootThenShootMiddleTopThenTopCenter(AutonomousCommandSelector autoSelector,
                                                                   Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectProvider,
                                                                   Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                                                                   Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                                                   PoseSubsystem pose, DriveSubsystem drive,
                                                                   Provider<PointAtSpeakerCommand> pointAtSpeakerCommandProvider,
                                                                   Provider<DriveToGivenNoteCommand> driveToGivenNoteCommandProvider,
                                                                   Provider<DriveToMidSpikeScoringLocationCommand> driveToMidSpikeScoringLocationProvider) {
        this.autoSelector = autoSelector;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (center)");
        var fireFirstNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireFirstNoteCommand));

        // Drive to middle spike note and collect
        queueMessageToAutoSelector("Drive to middle spike note, collect and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.SpikeMiddle);
                })
        );
        var driveToMiddleSpikeNoteAndCollect = driveToGivenNoteAndCollectProvider.get();
        this.addCommands(Commands.deadline(driveToMiddleSpikeNoteAndCollect));

        // Point at speaker
        var pointAtSpeakerFirst = pointAtSpeakerCommandProvider.get();

        // Fire Note into the speaker
        var fireSecondNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireSecondNoteCommand, pointAtSpeakerFirst));

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.SpikeTop);
                })
        );
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectProvider.get();
        this.addCommands(Commands.deadline(driveToTopSpikeNoteAndCollect));

        // Point at speaker
        var pointAtSpeakerSecond = pointAtSpeakerCommandProvider.get();

        // Fire Note into the speaker
        var fireThirdNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireThirdNoteCommand, pointAtSpeakerSecond));

        // Drive to top center note and collect
        queueMessageToAutoSelector("Drive to center top note, collect, drive back to middle spike location and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine1);
                })
        );
        var driveToTopCenterNoteAndCollect = driveToGivenNoteAndCollectProvider.get();
        this.addCommands(driveToTopCenterNoteAndCollect);

        // drive back to middle spike location
        var driveToMiddleSpikeScoringLocation = driveToMidSpikeScoringLocationProvider.get();
        this.addCommands(driveToMiddleSpikeScoringLocation);

        // Point at speaker
        var pointAtSpeakerThird = pointAtSpeakerCommandProvider.get();

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireFourthNoteCommand, pointAtSpeakerThird));
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }

}