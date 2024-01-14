package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.subsystems.drive.control_logic.HeadingModule;


import javax.inject.Inject;
import java.util.function.Supplier;

public class DriveToAmpCommmand extends BaseCommand {
    DriveSubsystem drive;
    PoseSubsystem pose;
    HeadingModule headingModule;


    private XYPair targetPosition =  new XYPair();
    private double targetHeading = 0;



    @Inject
    public DriveToAmpCommmand(DriveSubsystem drive, PoseSubsystem pose, HeadingModule.HeadingModuleFactory headingModuleFactory) {
        this.drive =  drive;
        this.pose = pose;
        headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());


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
        return drive.getPositionalPid().isOnTarget() && headingModule.isOnTarget();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.stop();
    }

    public void setTargetPosition(XYPair targetPosition) {
        this.targetPosition = targetPosition;
    }

    public void setTargetHeading(double heading) {
        this.targetHeading = heading;
    }
}