package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class DriveToNearestGoodScoringPositionCommand extends SwerveSimpleTrajectoryCommand {
    PoseSubsystem pose;

    @Inject
    public DriveToNearestGoodScoringPositionCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
                                             HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.pose = pose;
    }

    @Override
    public void initialize() {
        // Gets the nearest good scoring location and moves there
        Pose2d nearestScoringPosition = pose.getNearestGoodScoringPosition();

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                nearestScoringPosition, 10));
        logic.setKeyPoints(points);
        logic.setEnableConstantVelocity(true);
        logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        super.initialize();
    }
}
