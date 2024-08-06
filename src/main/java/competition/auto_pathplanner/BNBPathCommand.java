package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class BNBPathCommand extends SequentialCommandGroup {

    @Inject
    public BNBPathCommand(PoseSubsystem pose, PathPlannerDriveSubsystem drive,
                          DriveSubsystem driveSubsystem, RobotContainer robotContainer) {

        var path = new BasePathPlannerCommand(drive, driveSubsystem,
                robotContainer.getBNBCommand());

        this.addCommands(path);

        this.addCommands(new Command() {
            @Override
            public void initialize() {
                System.out.println("Print Command Initialized.");
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        });

    }
}
