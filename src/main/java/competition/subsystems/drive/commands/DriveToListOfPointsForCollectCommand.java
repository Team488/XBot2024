package competition.subsystems.drive.commands;

import competition.auto_programs.TwoNoteGriefAuto;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DriveToListOfPointsForCollectCommand extends SwerveSimpleTrajectoryCommand {

    DynamicOracle oracle;
    DriveSubsystem drive;
    Supplier<List<XbotSwervePoint>> pointsSupplier;

    @Inject
    public DriveToListOfPointsForCollectCommand(DriveSubsystem drive, DynamicOracle oracle,
                                      PoseSubsystem pose, PropertyFactory pf,
                                      HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.drive = drive;
    }

    @Override
    public void initialize() {
        log.info("Intitializing");
        this.logic.setKeyPointsProvider(pointsSupplier);
        this.logic.setAimAtGoalDuringFinalLeg(true);
        this.logic.setDriveBackwards(true);
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

    public void addPointsSupplier(Supplier<List<XbotSwervePoint>> pointsSupplier) {
        this.pointsSupplier = pointsSupplier;
    }
}