package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class Bot4NoteShootingFarCommandGroup extends SequentialCommandGroup {

    @Inject
    public Bot4NoteShootingFarCommandGroup(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                                DriveSubsystem driveSubsystem, RobotContainer robotContainer) {

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        var path = new BasePathPlannerCommand(drive, driveSubsystem, pose,
                robotContainer, robotContainer.getFast4NoteFarCommand());

        this.addCommands(path);

    }
}
