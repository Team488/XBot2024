package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToBottomSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsForCollectCommand;
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

public class SubwooferShotFromBotShootThenShootSpikes extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public SubwooferShotFromBotShootThenShootSpikes(AutonomousCommandSelector autoSelector,
                                                    Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                                    Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
                                                    Provider<DriveToBottomSubwooferCommand> driveToBottomSubwooferCommandProvider,
                                                    PoseSubsystem pose, DriveSubsystem drive,
                                                    Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                                    Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
                                                    Provider<DriveToListOfPointsForCollectCommand> driveToListOfPointsForCollectCommandProvider) {
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

        // Drive to middle spike note and collect
        queueMessageToAutoSelector("Drive to middle spike note, collect, drive back to sub(bottom) and shoot");
        var driveToMiddleSpikeNote = driveToListOfPointsForCollectCommandProvider.get();
        driveToMiddleSpikeNote.addPointsSupplier(this::goToBottomWhiteLineThenSpikeMiddle);
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

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect, drive back to sub(bottom) and shoot");
        var driveToTopSpikeNote = driveToListOfPointsForCollectCommandProvider.get();
        driveToTopSpikeNote.addPointsSupplier(this::goToBottomWhiteLineThenSpikeTop);
        var collectSequenceTop = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectSequenceTop, driveToTopSpikeNote));
//        this.addCommands(Commands.deadline(driveToTopSpikeNote, collectSequenceTop));

        // Drive back to subwoofer
        var driveBackToBottomSubwooferThird = driveToListOfPointsCommandProvider.get();
        driveBackToBottomSubwooferThird.addPointsSupplier(this::goBackToSubwoofer);
        this.addCommands(driveBackToBottomSubwooferThird);

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFourthNoteCommand);
    }

    public List<XbotSwervePoint> goToBottomWhiteLineThenSpikeMiddle() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeBottomWhiteLine.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSpikeMiddle, 10));
        return points;
    }

    public List<XbotSwervePoint> goToBottomWhiteLineThenSpikeTop() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeBottomWhiteLine.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSpikeTop, 10));
        return points;
    }

    public List<XbotSwervePoint> goBackToSubwoofer() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeBottomWhiteLine.getTranslation(),
                PoseSubsystem.BlueSubwooferBottomScoringLocation.getRotation(), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSubwooferBottomScoringLocation, 10));
        return points;
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
}