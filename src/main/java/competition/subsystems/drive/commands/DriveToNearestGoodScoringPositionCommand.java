package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;


public class DriveToNearestGoodScoringPositionCommand extends SwerveSimpleTrajectoryCommand {

    DynamicOracle oracle;
    DriveSubsystem drive;

    @Inject
    public DriveToNearestGoodScoringPositionCommand(DriveSubsystem drive, DynamicOracle oracle,
                                                    PoseSubsystem pose, PropertyFactory pf,
                                                    HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.drive = drive;
    }

    @Override
    public void initialize() {
        log.info("Initializing");

        var nearestScoringLocation = oracle.getNearestScoringLocation();

        if (nearestScoringLocation == null) {
            cancel();
        }

        var scoringLocationPose = nearestScoringLocation.getLocation();

        if (scoringLocationPose == null) {
            cancel();
        }

        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(new XbotSwervePoint(nearestScoringLocation., 10));
        this.logic.setKeyPoints(swervePoints);
        this.logic.setEnableConstantVelocity(true);
        this.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        log.info("Nearest location: " + nearestScoringLocation);
        log.info("Nearest location Pose: " + scoringLocationPose);

        super.initialize();
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }

}
