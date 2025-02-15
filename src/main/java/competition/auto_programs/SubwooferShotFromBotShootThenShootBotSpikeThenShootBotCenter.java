package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToBottomSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsForCollectCommand;
import competition.subsystems.pose.PoseSubsystem;
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

public class SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter(
            AutonomousCommandSelector autoSelector,
            Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
            Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
            Provider<DriveToBottomSubwooferCommand> driveToBottomSubwooferCommandProvider,
            PoseSubsystem pose, DriveSubsystem drive, CollectSequenceCommandGroup collectSequence,
            DriveToListOfPointsForCollectCommand driveToBottomCenterNote,
            DriveToListOfPointsCommand driveBackToBottomSubwoofer
            ) {
        this.autoSelector = autoSelector;

        // Force our location
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (bottom)");
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        var fireFirstNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFirstNoteCommand);

        // Drive to bottom spike note and collect
        queueMessageToAutoSelector("Drive to bottom spike note, collect, drive back to sub(bottom) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeBottom);
                })
        );
        var driveToBottomSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToBottomSpikeNoteAndCollect);

        // Drive back to subwoofer
        var driveBackToBottomSubwooferFirst = driveToBottomSubwooferCommandProvider.get();
        this.addCommands(driveBackToBottomSubwooferFirst);

        // Fire second note into the speaker
        var fireSecondNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireSecondNoteCommand);

        // Drive to bottom center note and collect
        queueMessageToAutoSelector("Drive to bottom center note, collect, drive back to sub(bottom) and shoot");
        driveToBottomCenterNote.addPointsSupplier(this::goToBottomCenterLine);
        this.addCommands(Commands.deadline(collectSequence, driveToBottomCenterNote));

        // Drive back to subwoofer
        driveBackToBottomSubwoofer.addPointsSupplier(this::goBackToSubwoofer);
        this.addCommands(driveBackToBottomSubwoofer);

        // Fire third note into the speaker
        var fireThirdNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireThirdNoteCommand);
    }

    public List<XbotSwervePoint> goToBottomCenterLine() {
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(PoseSubsystem.BlueSpikeBottomWhiteLine.getX(), PoseSubsystem.BlueSpikeBottomWhiteLine.getY() - 1);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation,
                PoseSubsystem.BlueSubwooferBottomScoringLocation.getRotation(), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine5, 10));
        return points;
    }

    public List<XbotSwervePoint> goBackToSubwoofer() {
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(PoseSubsystem.BlueSpikeBottomWhiteLine.getX(), PoseSubsystem.BlueSpikeBottomWhiteLine.getY() - 1.1);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation,
                PoseSubsystem.BlueSubwooferBottomScoringLocation.getRotation(), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSubwooferBottomScoringLocation, 10));
        return points;
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
}
