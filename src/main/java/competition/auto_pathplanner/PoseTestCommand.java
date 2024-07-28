package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class PoseTestCommand extends SequentialCommandGroup {

    @Inject
    public PoseTestCommand(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                           DriveSubsystem driveSubsystem, RobotContainer robotContainer) {

        var path = new BasePathPlannerCommand(drive, driveSubsystem, pose,
                robotContainer, robotContainer.getPoseTestCommand());

        this.addCommands(path);

    }
}
