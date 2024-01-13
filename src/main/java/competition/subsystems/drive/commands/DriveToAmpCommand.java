package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;


import javax.inject.Inject;

public class DriveToAmpCommmand extends BaseCommand {
    DriveSubsystem drive;
    PoseSubsystem pose;
    HeadingModule headingModule;
    @Inject
    public DriveToAmpCommmand(DriveSubsystem drive, PoseSubsystem pose, HeadingModule headingModuleFactory) {
        this.drive =  drive;
        this.pose = pose;
        this.headingModule = headingModuleFactory.create();


        this.addRequirements(drive);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void execute() {

    }

    @Override
    public boolean isFinished() {
        return
    }
}