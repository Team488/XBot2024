package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class SetPoseCommand extends SequentialCommandGroup {

    @Inject
    public SetPoseCommand(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                          DriveSubsystem driveSubsystem, RobotContainer robotContainer) {

        var startingPose= pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(robotContainer.getTranslationXYPose()));
        this.addCommands(startingPose);

    }
}
