package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
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

public class SubwooferShotFromMidShootThenShootNearestThree extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;
    double interstageTimeout = 3.5;

    @Inject
    public SubwooferShotFromMidShootThenShootNearestThree(AutonomousCommandSelector autoSelector,
                                                          Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                                          Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
                                                          Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                                          PoseSubsystem pose, DriveSubsystem drive,
                                                          DriveToListOfPointsCommand driveToBottomWhiteLine,
                                                          CollectSequenceCommandGroup collectBottomNote) {
        this.autoSelector = autoSelector;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (middle)");
        var fireFirstNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFirstNoteCommand);

        // Drive to middle spike note and collect
        queueMessageToAutoSelector("Drive to bottom spike note, collect, drive back to sub(middle) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeMiddle);
                })
        );
        var driveToMiddleSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToMiddleSpikeNoteAndCollect.withTimeout(interstageTimeout));

        // Drive back to subwoofer
        var driveBackToCentralSubwooferFirst = driveToCentralSubwooferCommandProvider.get();
        this.addCommands(driveBackToCentralSubwooferFirst.withTimeout(interstageTimeout));

        // Fire second note into the speaker
        var fireSecondNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireSecondNoteCommand);

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect, drive back to sub(middle) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeTop);
                })
        );
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopSpikeNoteAndCollect.withTimeout(interstageTimeout));

        // Drive back to subwoofer
        var driveBackToCentralSubwooferSecond = driveToCentralSubwooferCommandProvider.get();
        this.addCommands(driveBackToCentralSubwooferSecond.withTimeout(interstageTimeout));

        // Fire Note into the speaker
        var fireThirdNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireThirdNoteCommand);

        // Drive to bottom spike note and collect
        queueMessageToAutoSelector("Drive to bottom spike note, collect, drive back to sub(middle) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeBottom);
                })
        );

        // Need to drive to an interstitial point first
        //driveToBottomWhiteLine.addPointsSupplier(this::goToBottomWhiteLine);
        //driveToBottomWhiteLine.logic.setStopWhenFinished(false);

        //this.addCommands(driveToBottomWhiteLine);

        // Now, go get the bottom spike note
        var driveToBottomSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToBottomSpikeNoteAndCollect.withTimeout(interstageTimeout));

        // Drive back to subwoofer
        var driveBackToCentralSubwooferThird = driveToCentralSubwooferCommandProvider.get();
        this.addCommands(driveBackToCentralSubwooferThird.withTimeout(interstageTimeout));

        // Fire Note into the speaker
        var fireFourthNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFourthNoteCommand);
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }

    public List<XbotSwervePoint> goToBottomWhiteLine() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSpikeBottomWhiteLine.getTranslation(),
                Rotation2d.fromDegrees(180), 10));
        return points;
    }
}