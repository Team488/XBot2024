package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToCentralSubwooferAndFireIfHasNoteCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
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
    CollectorSubsystem collector;

    @Inject
    public SubwooferShotFromMidShootThenShootNearestThree(AutonomousCommandSelector autoSelector,
                                                          Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                                          Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
                                                          Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                                          PoseSubsystem pose, DriveSubsystem drive, CollectorSubsystem collector,
                                                          Provider<DriveToCentralSubwooferAndFireIfHasNoteCommandGroup>
                                                                      driveToCentralSubwooferAndFireIfHasNoteCommandGroupProvider) {
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

        // Go back and fire if has note
        var driveBackIfNoteAndFireFirst = driveToCentralSubwooferAndFireIfHasNoteCommandGroupProvider.get();
        this.addCommands(driveBackIfNoteAndFireFirst);

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect, drive back to sub(middle) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeTop);
                })
        );
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopSpikeNoteAndCollect.withTimeout(interstageTimeout));

        // Go back and fire if has note
        var driveBackIfNoteAndFireSecond = driveToCentralSubwooferAndFireIfHasNoteCommandGroupProvider.get();
        this.addCommands(driveBackIfNoteAndFireSecond);



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

        // Go back and fire if has note
        var driveBackIfNoteAndFireThird = driveToCentralSubwooferAndFireIfHasNoteCommandGroupProvider.get();
        this.addCommands(driveBackIfNoteAndFireThird);
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