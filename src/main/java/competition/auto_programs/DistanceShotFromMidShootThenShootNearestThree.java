package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
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

public class DistanceShotFromMidShootThenShootNearestThree extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public DistanceShotFromMidShootThenShootNearestThree(AutonomousCommandSelector autoSelector,
                                                         Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                                         Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                                                         Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                                         PoseSubsystem pose, DriveSubsystem drive,
                                                         Provider<PointAtSpeakerCommand> pointAtSpeakerCommandProvider,
                                                         FireFromSubwooferCommandGroup fireFromSubwooferCommandGroup,
                                                         Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                                         Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider) {
        this.autoSelector = autoSelector;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (center)");
        // Fire preload note into the speaker from starting position
        this.addCommands(fireFromSubwooferCommandGroup);

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect and shoot");
        var driveToTopSpikeNote = driveToListOfPointsCommandProvider.get();
        driveToTopSpikeNote.addPointsSupplier(this::goToTopSpike);
        var collectSequenceTop = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectSequenceTop, driveToTopSpikeNote));

        // this is only used for testing in the sim
//        this.addCommands(Commands.deadline(driveToTopSpikeNote, collectSequenceTop));

        // Fire Note into the speaker
        var fireSecondNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(fireSecondNoteCommand);

        // Drive to middle spike note and collect
        queueMessageToAutoSelector("Drive to middle spike note, collect and shoot");
        var driveToMidSpikeNote = driveToListOfPointsCommandProvider.get();
        driveToMidSpikeNote.addPointsSupplier(this::goToMidSpike);
        var collectSequenceMid = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectSequenceMid, driveToMidSpikeNote));

        // this is only used for testing in the sim
//        this.addCommands(Commands.deadline(driveToMidSpikeNote, collectSequenceMid));

        // Fire Note into the speaker
        var fireThirdNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(fireThirdNoteCommand);

        // Drive to bottom spike note and collect
        queueMessageToAutoSelector("Drive to bottom spike note, collect and shoot");
        var driveToBotSpikeNote = driveToListOfPointsCommandProvider.get();
        driveToBotSpikeNote.addPointsSupplier(this::goToBotSpike);
        var collectSequenceBot = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(collectSequenceBot, driveToBotSpikeNote));

        // this is only used for testing in the sim
//        this.addCommands(Commands.deadline(driveToBotSpikeNote, collectSequenceBot));

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireNoteCommandGroupProvider.get();
        this.addCommands(fireFourthNoteCommand);
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }

    public List<XbotSwervePoint> goToTopSpike() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeTop.getTranslation(),
                Rotation2d.fromDegrees(-153.64394), 10));
        return points;
    }

    public List<XbotSwervePoint> goToMidSpike() {
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(
                PoseSubsystem.BlueSpikeMiddle.getX() - 0.99,
                PoseSubsystem.BlueSpikeMiddle.getY() + 0.26956);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation,
                Rotation2d.fromDegrees(180), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeMiddle.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        return points;
    }

    public List<XbotSwervePoint> goToBotSpike() {
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(
                PoseSubsystem.BlueSpikeBottom.getX() - 0.97,
                PoseSubsystem.BlueSpikeBottom.getY() + 0.27556);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation,
                Rotation2d.fromDegrees(153.64394), 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeBottom.getTranslation(),
                Rotation2d.fromDegrees(153.64394), 10));
        return points;
    }

}