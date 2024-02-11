package competition.auto;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseCommand;
import xbot.common.math.WrappedRotation2d;

import javax.inject.Inject;

public class MidNoteCommand extends BaseCommand {
    PathPlannerDriveSubsystem drive;
    Command autonomousCommand;
    RobotContainer robotContainer;
    DriveSubsystem driveSubsystem;
    PoseSubsystem pose;

    @Inject
    public MidNoteCommand(PathPlannerDriveSubsystem drive, RobotContainer robotContainer,
                                  DriveSubsystem driveSubsystem, PoseSubsystem pose) {
        this.robotContainer = robotContainer;
        this.drive = drive;
        this.driveSubsystem = driveSubsystem;
        this.pose = pose;
        addRequirements(driveSubsystem);
        addRequirements(drive);
        this.autonomousCommand = robotContainer.getMidNoteCommand();
        autonomousCommand.addRequirements(drive);
        autonomousCommand.addRequirements(driveSubsystem);

    }

    @Override
    public void initialize() {
        log.info("Initializing");
        pose.setCurrentPosition(0.51, 4.07, new WrappedRotation2d(0));
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
