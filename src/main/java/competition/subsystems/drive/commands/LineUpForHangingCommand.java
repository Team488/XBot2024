package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class LineUpForHangingCommand extends SwerveSimpleTrajectoryCommand {

    PoseSubsystem pose;

    private Pose2d getClosestMagnitudeHangingPosition() {
        Pose2d currentPose = pose.getCurrentPose2d();

        // hang1 2 and 3 doesn't exist, need to record them somewhere
        // probably under pose
        double dist1 = calculateDistance(hang1.getX(), currentPose.getX(), hang1.getY(), currentPose.getY());
        double dist2 = calculateDistance(hang2.getX(), currentPose.getX(), hang2.getY(), currentPose.getY());
        double dist3 = calculateDistance(hang3.getX(), currentPose.getX(), hang3.getY(), currentPose.getY());

        // check a b c return corresponding Pose2d of lowest value?
        double lowest = dist1;
        if (dist2 < lowest) {
            lowest = dist2;
        }
        if (dist3 < lowest) {
            lowest = dist3;
        }

        // Use switch case probably...
        if (lowest == dist1) {
            return hang1;
        } else if (lowest == dist2) {
            return hang2;
        } else {
            return hang3;
        }
    }

    private double calculateDistance(double x1, double x2, double y1, double y2) {
        return Math.abs(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
    }

    @Inject
    LineUpForHangingCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
                            HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.pose = pose;
    }

    @Override
    public void initialize() {
        super.initialize();
        Pose2d closestHangingPose = getClosestMagnitudeHangingPosition();

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(closestHangingPose, 10));
        logic.setKeyPoints(points);
        logic.setEnableConstantVelocity(true);
        logic.setConstantVelocity(1);
    }
}
