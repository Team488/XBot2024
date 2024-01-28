package competition.auto;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class DriveCoupleFeetCommand extends BaseCommand {
    PathPlannerDriveSubsystem drive;
    Command autonomousCommand;
    RobotContainer robotContainer;
    DriveSubsystem driveSubsystem;

    @Inject
    public DriveCoupleFeetCommand(PathPlannerDriveSubsystem drive, RobotContainer robotContainer, DriveSubsystem driveSubsystem) {
        this.robotContainer = robotContainer;
        this.drive = drive;
        this.driveSubsystem = driveSubsystem;
        this.addRequirements(driveSubsystem);
        this.addRequirements(drive);
        this.autonomousCommand = robotContainer.getAutonomousCommand();

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
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.stop();
    }
}
