package competition.auto_pathplanner;

import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class UpdateHolonomicCommand extends BaseCommand {
    PathPlannerDriveSubsystem drive;
    @Inject
    public UpdateHolonomicCommand(PathPlannerDriveSubsystem drive) {
        this.drive = drive;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        drive.configureDriveSubsystem();
    }

    @Override
    public void execute() {
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
    }
}
