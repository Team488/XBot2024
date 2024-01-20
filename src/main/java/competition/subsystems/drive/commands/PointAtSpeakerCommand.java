package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import xbot.common.subsystems.drive.control_logic.HeadingModule;

import java.lang.Math;

public class PointAtSpeakerCommand extends BaseCommand {

    PoseSubsystem pose;
    DriveSubsystem drive;
    XYPair speakerPosition = new XYPair(-0.0381,5.547868);

    Pose2d currentPosition;
    HeadingModule headingModule;

    public PointAtSpeakerCommand(DriveSubsystem drive, PoseSubsystem pose, HeadingModule.HeadingModuleFactory headingModuleFactory){
        
    }



    double angle;
    @Override
    public void initialize() {
    currentPosition = pose.getCurrentPose2d();
    }

    @Override
    public void execute() {
        currentPosition = pose.getCurrentPose2d();

        angle = (90 + (180 - Math.tan((currentPosition.getX() - speakerPosition.x) / (currentPosition.getY() - speakerPosition.y))));

    }
}
