package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;
import java.lang.Math;

public class PointAtSpeakerCommand extends BaseCommand {

    PoseSubsystem pose;
    DriveSubsystem drive;
    XYPair speakerPosition = new XYPair(-0.0381,5.547868);

    Pose2d currentPosition;
    double angle;
    XYPair currentPositionCord;
    HeadingModule headingModule;

    @Inject
    public PointAtSpeakerCommand(DriveSubsystem drive, PoseSubsystem pose, HeadingModule.HeadingModuleFactory headingModuleFactory){
        this.drive = drive;
        this.pose = pose;
        headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
    }



    @Override
    public void initialize() {
    currentPosition = pose.getCurrentPose2d();
    currentPositionCord = new XYPair(currentPosition.getX(),currentPosition.getY());

        if (currentPositionCord.y > speakerPosition.y) {
            angle = (90 + (180 - Math.toDegrees(Math.atan(Math.abs((currentPosition.getX() - speakerPosition.x)) / Math.abs((currentPosition.getY() - speakerPosition.y))))));
        }
        else if (currentPositionCord.y < speakerPosition.y){
            angle = (90 + Math.toDegrees(Math.atan(Math.abs((currentPosition.getX() - speakerPosition.x)) / Math.abs((currentPosition.getY() - speakerPosition.y)))));
        }
        else{
            angle = 180;
        }
    }

    @Override
    public void execute() {
//        currentPosition = pose.getCurrentPose2d();
//        currentPositionCord.x = currentPosition.getX();
//        currentPositionCord.y = currentPosition.getY();
//
//        

        double headingPower = headingModule.calculateHeadingPower(angle);
        drive.move(currentPositionCord,headingPower);

    }

    @Override
    public boolean isFinished() {
        return headingModule.isOnTarget();
    }
}
