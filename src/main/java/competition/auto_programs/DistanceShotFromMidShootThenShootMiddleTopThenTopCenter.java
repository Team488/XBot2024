package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToMidSpikeScoringLocationCommand;
import competition.subsystems.drive.commands.PointAtSpeakerCommand;
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
                                                                   Provider<DriveToMidSpikeScoringLocationCommand> driveToMidSpikeScoringLocationProvider,
                                                                   Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                                                   Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider) {
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
                    drive.setTargetNote(PoseSubsystem.BlueSpikeMiddle);
                })
        );
        var driveToMiddleSpikeNoteAndCollect = driveToGivenNoteAndCollectProvider.get();
        this.addCommands(driveToMiddleSpikeNoteAndCollect);

        // Fire Note into the speaker
        var fireSecondNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(fireSecondNoteCommand);

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect and shoot");
        var driveToTopSpikeNote = driveToListOfPointsCommandProvider.get();
        driveToTopSpikeNote.addPointsSupplier(this::goToTopSpike);
        var collectTop = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectTop, driveToTopSpikeNote));

        // this is only used for testing in the sim
//        this.addCommands(Commands.deadline(driveToTopSpikeNote, collectTop));

        // Fire Note into the speaker
        var fireThirdNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(Commands.deadline(fireThirdNoteCommand));

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
        queueMessageToAutoSelector("Drive to top spike note, collect and shoot");
        var driveBackToMiddleSpikeNote = driveToListOfPointsCommandProvider.get();
        driveBackToMiddleSpikeNote.addPointsSupplier(this::goToBackToMidSpike);
        this.addCommands(driveBackToMiddleSpikeNote);

        // this is only used for testing in the sim
//        this.addCommands(driveBackToMiddleSpikeNote);

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(fireFourthNoteCommand);
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }

    public List<XbotSwervePoint> goToTopSpike() {
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(
                PoseSubsystem.BlueSpikeMiddle.getX() - 0.99,
                PoseSubsystem.BlueSpikeMiddle.getY() + 0.26956);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation,
                Rotation2d.fromDegrees(153.64394), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeTop.getTranslation(),
                Rotation2d.fromDegrees(-153.64394), 10));
        return points;
    }

    public List<XbotSwervePoint> goToBackToMidSpike() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeMiddle.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        return points;
    }

}