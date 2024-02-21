package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class StopDriveCommand extends BaseCommand {

    final DriveSubsystem drive;

    @Inject
    public StopDriveCommand(DriveSubsystem drive) {
        this.drive = drive;
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        drive.stop();
    }
}
