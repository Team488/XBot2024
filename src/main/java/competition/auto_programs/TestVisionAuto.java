package competition.auto_programs;

import competition.commandgroups.DriveToGivenNoteWithVisionCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import org.kobe.xbot.Client;

import javax.inject.Inject;

public class TestVisionAuto extends SequentialCommandGroup {

    @Inject
    public TestVisionAuto(
            DriveSubsystem drive,
            PoseSubsystem pose,
            DriveToWaypointsWithVisionCommand driveToWaypointsWithVisionCommand
    ) {


        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeMiddle);
                })
        );

        this.addCommands(driveToWaypointsWithVisionCommand);
    }
}
