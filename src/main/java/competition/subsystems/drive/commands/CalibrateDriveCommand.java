package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class CalibrateDriveCommand extends BaseCommand {

    DriveSubsystem drive;

    @Inject
    public CalibrateDriveCommand(DriveSubsystem drive) {
        this.drive = drive;
        requires(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        drive.move(new XYPair(-0.25, 0), 0);
    }
}
