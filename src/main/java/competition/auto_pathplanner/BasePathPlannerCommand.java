package competition.auto_pathplanner;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class BasePathPlannerCommand extends BaseCommand {
    PathPlannerDriveSubsystem drive;
    Command autonomousCommand;
    DriveSubsystem driveSubsystem;

    @Inject
    public BasePathPlannerCommand(PathPlannerDriveSubsystem drive, DriveSubsystem driveSubsystem,
                                  Command autonomousCommand) {
        this.drive = drive;
        this.driveSubsystem = driveSubsystem;
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
    public boolean isFinished() {
        return autonomousCommand.isFinished();
    }

    @Override
    public void end(boolean interrupted) {
        log.info("Command has ended");
        super.end(interrupted);
        drive.stop();
        driveSubsystem.stop();
    }
}
