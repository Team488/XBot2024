package competition.subsystems.drive.commands;

import xbot.common.command.BaseCommand;

public class PointAtSpeakerCommand extends BaseCommand {
    private int theta;
    @Override
    public void initialize() {

    }

    @Override
    public void execute() {
        theta = 180 + pose.getCurrentHeading().getDegrees();
    }
}
