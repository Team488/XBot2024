package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Twist2d;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.SwerveSimpleTrajectoryLogic;


import javax.inject.Inject;
import java.util.function.Supplier;

public class DriveToAmpCommmand extends BaseCommand {
    DriveSubsystem drive;
    PoseSubsystem pose;
    HeadingModule headingModule;

    public SwerveSimpleTrajectoryLogic logic;

    private XYPair targetPosition;
    private double targetHeading;



    @Inject
    public DriveToAmpCommmand(DriveSubsystem drive, PoseSubsystem pose, HeadingModule.HeadingModuleFactory headingModuleFactory) {
        this.drive =  drive;
        this.pose = pose;
        headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());

        this.addRequirements(drive);

        logic = new SwerveSimpleTrajectoryLogic();
    }

    @Override
    public void initialize() {
        targetPosition  = new XYPair(1.855, 7.781); //blue amp (14.732, 7.781)
        targetHeading = 90;  //blue amp

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

        drive.fieldOrientedDrive(intent, headingPower, pose.getCurrentHeading().getDegrees(), false);

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