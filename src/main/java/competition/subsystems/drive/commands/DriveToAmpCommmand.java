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


    private XYPair targetPosition;
    private double targetHeading;



    @Inject
    public DriveToAmpCommmand(DriveSubsystem drive, PoseSubsystem pose, HeadingModule.HeadingModuleFactory headingModuleFactory) {
        this.drive =  drive;
        this.pose = pose;
        headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());

        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        targetPosition  = null; //get april tag XYPair
        targetHeading = 0;  //get april tag heading

    }

    @Override
    public void execute() {

        //goalVector is the x, y difference from current robot position on the field and target position
        XYPair goalVector = targetPosition.clone()
                .add(pose.getCurrentFieldPose().getPoint().scale(-1));

        //turns vector into a distance (length)
        double goalMagnitude = goalVector.getMagnitude();

        //as goalMagnitude becomes less, drivePower also does
        double drivePower = drive.getPositionalPid().calculate(goalMagnitude, 0);

        //the polar coordinate of goalVector, (x = angle, y = distance),
        //distance in this case is power
        XYPair intent = XYPair.fromPolar(goalVector.getAngle(), drivePower);

        double headingPower = headingModule.calculateHeadingPower(targetHeading);

        drive.fieldOrientedDrive(intent, headingPower, pose.getCurrentHeading().getDegrees(), true);
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
}