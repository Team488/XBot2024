package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class XYandRotateCommand extends SequentialCommandGroup {

    @Inject
    public XYandRotateCommand(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                                   DriveSubsystem driveSubsystem, RobotContainer robotContainer) {

        var path = new BasePathPlannerCommand(drive, driveSubsystem,
                robotContainer.getTranslationXandYRotate());

        this.addCommands(path);

    }
}
