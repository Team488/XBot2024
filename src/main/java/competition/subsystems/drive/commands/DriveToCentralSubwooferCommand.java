package competition.subsystems.drive.commands;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;


public class DriveToCentralSubwooferCommand extends SwerveSimpleTrajectoryCommand {

    DynamicOracle oracle;
    DriveSubsystem drive;
    PoseSubsystem pose;
    DoubleProperty meterThreshold;
    DoubleProperty velocityThreshold;
    Translation2d goal;
    CollectorSubsystem collector;

    @Inject
    public DriveToCentralSubwooferCommand(DriveSubsystem drive, DynamicOracle oracle,
                                          PoseSubsystem pose, PropertyFactory pf,
                                          HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          CollectorSubsystem collector) {
        super(drive, pose, pf, headingModuleFactory);

        // TODO: Potentially adjust the values?
        meterThreshold = pf.createPersistentProperty("DriveToSubwooferMeterThreshold", 0.3048);
        velocityThreshold = pf.createPersistentProperty("DriveToSubwooferVelocityThreshold", 0.2);

        this.oracle = oracle;
        this.drive = drive;
        this.pose = pose;
        this.collector = collector;
        addRequirements(collector);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();

        // When the robot goes to the central subwoofer to score, it can score further back. This saves time.
        Translation2d translation = new Translation2d(
                PoseSubsystem.BlueSubwooferMiddleScoringLocation.getX() + 0.0762,
                PoseSubsystem.BlueSubwooferMiddleScoringLocation.getY());
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                translation, Rotation2d.fromDegrees(180), 10));
        this.logic.setKeyPoints(swervePoints);
        this.logic.setEnableConstantVelocity(true);
        this.logic.setConstantVelocity(drive.getSuggestedAutonomousMaximumSpeed());
        // this is commented out because we want our autonomous to be very basic right now
//        this.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
        this.goal = translation;
        super.initialize();
    }

    @Override
    public void execute() {
        collector.intake();
        super.execute();
    }

    @Override
    public boolean isFinished() {
        double speed = pose.getRobotCurrentSpeed();

        Translation2d robotLocation = pose.getCurrentPose2d().getTranslation();

        // Returns finished if both position and velocity are under threshold
        boolean nearPositionThreshold = PoseSubsystem.convertBlueToRedIfNeeded(
                goal).getDistance(robotLocation) < meterThreshold.get();

        boolean nearVelocityThreshold = speed < velocityThreshold.get();

        return (nearPositionThreshold && nearVelocityThreshold) || super.isFinished();
    }
}
