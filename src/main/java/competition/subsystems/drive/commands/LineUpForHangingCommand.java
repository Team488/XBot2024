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
    Pose2d hang1 = PoseSubsystem.BlueStageNWHangingPreparationPoint;
    Pose2d hang2 = PoseSubsystem.BlueStageSWHangingPreparationPoint;
    Pose2d hang3 = PoseSubsystem.BlueStageEHangingPreparationPoint;

    private Pose2d getClosestMagnitudeHangingPosition() {
        Pose2d currentPose = pose.getCurrentPose2d();

        // Get distance between currentPose and hangingPoses
        double dist1 = hang1.getTranslation().getDistance(currentPose.getTranslation());
        double dist2 = hang2.getTranslation().getDistance(currentPose.getTranslation());
        double dist3 = hang3.getTranslation().getDistance(currentPose.getTranslation());

        // Return closest Pose2d
        Pose2d closestPose = hang1;
        double leastDistance = dist1;

        if (dist2 < leastDistance) {
            leastDistance = dist2;
            closestPose = hang2;
        }
        if (dist3 < leastDistance) {
            closestPose = hang3;
        }

        return closestPose;
    }

    @Inject
    LineUpForHangingCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
                            HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.pose = pose;
    }

    @Override
    public void initialize() {

        // Get the closest hanging position
        Pose2d closestHangingPose = getClosestMagnitudeHangingPosition();

        // Set up swerve points to the position
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(closestHangingPose, 10));
        logic.setKeyPoints(points);
        logic.setEnableConstantVelocity(true);
        logic.setConstantVelocity(3);

        super.initialize();
    }
}