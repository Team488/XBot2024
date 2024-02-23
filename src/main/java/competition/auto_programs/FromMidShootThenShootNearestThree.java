package competition.auto_programs;

import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import javax.inject.Provider;

public class FromMidShootThenShootNearestThree extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;


    public FromMidShootThenShootNearestThree(AutonomousCommandSelector autoSelector,
                                             Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                             Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                                             Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                             PoseSubsystem pose, DriveSubsystem drive) {
        this.autoSelector = autoSelector;

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SubwooferCentralScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire note into the speaker from starting position
        var fireNoteIntoSpeakerFromStartingPosition = fireNoteCommandGroupProvider.get();
        this.addCommands(fireNoteIntoSpeakerFromStartingPosition);

        // Drive to top spike note and collect
        drive.setTargetNote(BasePoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SpikeTop));
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopSpikeNoteAndCollect);

        // Drive back to subwoofer
        var driveBackToCentralSubwoofer = driveToCentralSubwooferCommandProvider.get();
        this.addCommands(driveBackToCentralSubwoofer);


    }
}
