package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;


public class DriveToTopSubwooferCommand extends SwerveSimpleTrajectoryCommand {

    DynamicOracle oracle;
    DriveSubsystem drive;

    @Inject
    public DriveToTopSubwooferCommand(DriveSubsystem drive, DynamicOracle oracle,
                                          PoseSubsystem pose, PropertyFactory pf,
                                          HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.drive = drive;
    }

    @Override
    public void initialize() {
        log.info("Intitializing");
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        Translation2d translation = new Translation2d(
                PoseSubsystem.BlueSubwooferTopScoringLocation.getX() + 0.0762,
                PoseSubsystem.BlueSubwooferTopScoringLocation.getY());
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation, PoseSubsystem.BlueSubwooferTopScoringLocation.getRotation(), 10));
        this.logic.setKeyPoints(swervePoints);
        this.logic.setEnableConstantVelocity(true);
        this.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());
        // this is commented out because we want our autonomous to be very basic right now
//        this.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
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
