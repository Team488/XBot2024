package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class BNBCHOREOCommand extends SequentialCommandGroup {

    @Inject
    public BNBCHOREOCommand(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                          DriveSubsystem driveSubsystem, RobotContainer robotContainer) {

            var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        var path = new BasePathPlannerCommand(drive, driveSubsystem,
                robotContainer.getBNBChoreo());

        this.addCommands(path);

    }
}
