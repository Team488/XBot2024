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

    @Inject
    public PointAtSpeakerCommand(DriveSubsystem drive, PoseSubsystem pose){
        this.drive = drive;
        this.pose = pose;

    }



    @Override
    public void initialize() {
    currentPosition = pose.getCurrentPose2d();
    currentPositionCord = new XYPair(currentPosition.getX(),currentPosition.getY());
    }

    @Override
    public void execute() {
        currentPosition = pose.getCurrentPose2d();
        currentPositionCord.x = currentPosition.getX();
        currentPositionCord.y = currentPosition.getY();
        angle = (90 + (180 - Math.atan((currentPosition.getX() - speakerPosition.x) / (currentPosition.getY() - speakerPosition.y))));
        drive.setDesiredHeading(angle);
        drive.move(currentPositionCord,angle);

    }
}
