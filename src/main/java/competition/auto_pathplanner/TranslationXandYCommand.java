package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class TranslationXandYCommand extends SequentialCommandGroup {

    @Inject
    public TranslationXandYCommand(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                               DriveSubsystem driveSubsystem, RobotContainer robotContainer) {


        var path = new BasePathPlannerCommand(drive, driveSubsystem, pose,
                robotContainer, robotContainer.getTranslationXandY());

        this.addCommands(path);

    }
}
