package competition.subsystems.drive.commands;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;
import javax.inject.Inject;
import java.util.ArrayList;

public class LineUpForHangingCommand extends SwerveSimpleTrajectoryCommand {

    PoseSubsystem pose;
    Pose2d hang1 = PoseSubsystem.BlueTopHangingLineUp;
    Pose2d hang2 = PoseSubsystem.BlueBottomHangingLineUp;
    Pose2d hang3 = PoseSubsystem.BlueHangFromBackLineUp;

    private double calculateDistance(double x1, double x2, double y1, double y2) {
        return Math.abs(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
    }

    private Pose2d getClosestMagnitudeHangingPosition() {
        Pose2d currentPose = pose.getCurrentPose2d();

        double dist1 = calculateDistance(hang1.getX(), currentPose.getX(), hang1.getY(), currentPose.getY());
        double dist2 = calculateDistance(hang2.getX(), currentPose.getX(), hang2.getY(), currentPose.getY());
        double dist3 = calculateDistance(hang3.getX(), currentPose.getX(), hang3.getY(), currentPose.getY());

        double leastDistance = dist1;
        if (dist2 < leastDistance) {
            leastDistance = dist2;
        }
        if (dist3 < leastDistance) {
            leastDistance = dist3;
        }

        if (leastDistance == dist1) {
            return hang1;
        } else if (leastDistance == dist2) {
            return hang2;
        } else {
            return hang3;
        }
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

        // Get the closest hanging position from where we currently are.
        Pose2d closestHangingPose = getClosestMagnitudeHangingPosition();

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(closestHangingPose, 10));
        logic.setKeyPoints(points);
        logic.setEnableConstantVelocity(true);
        logic.setConstantVelocity(1);
    }
}