package competition.auto_programs;

import competition.commandgroups.DriveToWaypointsWithVisionCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class TestVisionAuto extends SequentialCommandGroup {

    @Inject
    public TestVisionAuto(
            DriveSubsystem drive,
            PoseSubsystem pose,
            DriveToWaypointsWithVisionCommand driveToWaypointsWithVisionCommand
    ) {
        this.addCommands(driveToWaypointsWithVisionCommand);
    }
}
