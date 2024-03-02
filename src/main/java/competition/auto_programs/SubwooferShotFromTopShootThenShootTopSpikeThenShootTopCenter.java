package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToBottomSubwooferCommand;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsForCollectCommand;
import competition.subsystems.drive.commands.DriveToTopSubwooferCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter(
            AutonomousCommandSelector autoSelector,
            Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
            Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
            Provider<DriveToTopSubwooferCommand> driveToTopSubwooferCommandProvider,
            PoseSubsystem pose, DriveSubsystem drive, CollectSequenceCommandGroup collectSequence,
            DriveToListOfPointsForCollectCommand driveToBottomCenterNote,
            DriveToListOfPointsCommand driveBackToBottomSubwoofer
    ) {
        this.autoSelector = autoSelector;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        var fireFirstNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFirstNoteCommand);

        // Drive to top spike note and collect
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.SpikeTop);
                })
        );
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopSpikeNoteAndCollect);

        // Drive back to subwoofer
        var driveBackToTopSubwooferFirst = driveToTopSubwooferCommandProvider.get();
        this.addCommands(driveBackToTopSubwooferFirst);

        // Fire second note into the speaker
        var fireSecondNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireSecondNoteCommand);

        // Drive to top center note and collect
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine1);
                })
        );
        var driveToTopCenterNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopCenterNoteAndCollect);

        // Drive back to subwoofer
        var driveBackToTopSubwooferSecond = driveToTopSubwooferCommandProvider.get();
        this.addCommands(driveBackToTopSubwooferSecond);

        // Fire third note into the speaker
        var fireThirdNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireThirdNoteCommand);
    }
}
