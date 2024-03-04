package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsForCollectCommand;
import competition.subsystems.drive.commands.DriveToTopSubwooferCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class SubwooferShotFromTopShootThenShootSpikes extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;
    PoseSubsystem pose;

    @Inject
    public SubwooferShotFromTopShootThenShootSpikes(AutonomousCommandSelector autoSelector,
                                                    Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                                    Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
                                                    Provider<DriveToTopSubwooferCommand> driveToTopSubwooferCommandProvider,
                                                    PoseSubsystem pose, DriveSubsystem drive,
                                                    Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                                    Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
                                                    Provider<DriveToListOfPointsForCollectCommand> driveToListOfPointsForCollectCommandProvider) {
        this.autoSelector = autoSelector;
        this.pose = pose;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (top)");
        var fireFirstNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFirstNoteCommand);

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect, drive back to sub(top) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeTop);
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

        // Drive to middle spike note and collect
        queueMessageToAutoSelector("Drive to middle spike note, collect, drive back to sub(top) and shoot");
        var driveToMiddleSpikeNote = driveToListOfPointsForCollectCommandProvider.get();
        driveToMiddleSpikeNote.addPointsSupplier(this::goToTopWhiteLineThenSpikeMiddle);
        var collectSequenceMid = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectSequenceMid, driveToMiddleSpikeNote));

        // this is only used for testing in the sim
//        this.addCommands(Commands.deadline(driveToMiddleSpikeNote, collectSequenceMid));


        // Drive back to subwoofer
        var driveBackToBottomSubwooferSecond = driveToListOfPointsCommandProvider.get();
        driveBackToBottomSubwooferSecond.addPointsSupplier(this::goBackToSubwoofer);
        this.addCommands(driveBackToBottomSubwooferSecond);

        // Fire Note into the speaker
        var fireThirdNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireThirdNoteCommand);

        // Drive to bottom spike note and collect
        queueMessageToAutoSelector("Drive to bottom spike note, collect, drive back to sub(top) and shoot");
        var driveToBottomSpikeNote = driveToListOfPointsForCollectCommandProvider.get();
        driveToBottomSpikeNote.addPointsSupplier(this::goToTopWhiteLineThenSpikeBottom);
        var collectSequenceTop = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectSequenceTop, driveToBottomSpikeNote));
//        this.addCommands(Commands.deadline(driveToTopSpikeNote, collectSequenceTop));

        // Drive back to subwoofer
        var driveBackToBottomSubwooferThird = driveToListOfPointsCommandProvider.get();
        driveBackToBottomSubwooferThird.addPointsSupplier(this::goBackToSubwoofer);
        this.addCommands(driveBackToBottomSubwooferThird);

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFourthNoteCommand);
    }

    public List<XbotSwervePoint> goToTopWhiteLineThenSpikeMiddle() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeTopWhiteLine.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSpikeMiddle, 10));
        return points;
    }

    public List<XbotSwervePoint> goToTopWhiteLineThenSpikeBottom() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeTopWhiteLine.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSpikeBottom, 10));
        return points;
    }

    public List<XbotSwervePoint> goBackToSubwoofer() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeTopWhiteLine.getTranslation(),
                PoseSubsystem.BlueSubwooferTopScoringLocation.getRotation(), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSubwooferTopScoringLocation, 10));
        return points;
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }

}