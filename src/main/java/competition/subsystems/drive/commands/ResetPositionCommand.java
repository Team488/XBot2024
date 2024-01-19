package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ResetPositionCommand extends BaseCommand {

    PoseSubsystem pose;

    @Inject
    public ResetPositionCommand(PoseSubsystem pose) {
        this.pose = pose;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        pose.setCurrentPosition(0,0);
    }

    @Override
    public void execute() {

    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
