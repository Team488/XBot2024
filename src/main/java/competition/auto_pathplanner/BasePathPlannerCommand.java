package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class BasePathPlannerCommand extends BaseCommand {
    PathPlannerDriveSubsystem drive;
    Command autonomousCommand;
    RobotContainer robotContainer;
    DriveSubsystem driveSubsystem;
    PoseSubsystem pose;

    @Inject
    public BasePathPlannerCommand(PathPlannerDriveSubsystem drive, DriveSubsystem driveSubsystem,
                                  PoseSubsystem pose, RobotContainer robotContainer, Command autonomousCommand) {
        this.robotContainer = robotContainer;
        this.drive = drive;
        this.driveSubsystem = driveSubsystem;
        this.pose = pose;
        addRequirements(driveSubsystem);
        addRequirements(drive);
        this.autonomousCommand = autonomousCommand;
        autonomousCommand.addRequirements(drive);
        autonomousCommand.addRequirements(driveSubsystem);
        setName(autonomousCommand.getName());
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        autonomousCommand.schedule();
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        return autonomousCommand.isFinished();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.stop();
        driveSubsystem.stop();
    }
}
