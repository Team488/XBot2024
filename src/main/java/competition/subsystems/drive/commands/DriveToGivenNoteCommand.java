package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;


public class DriveToGivenNoteCommand extends SwerveSimpleTrajectoryCommand {

    DynamicOracle oracle;
    DriveSubsystem drive;
    public Translation2d[] waypoints = null;

    @Inject
    public DriveToGivenNoteCommand(DriveSubsystem drive, DynamicOracle oracle,
                                   PoseSubsystem pose, PropertyFactory pf,
                                   HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.drive = drive;
    }

    @Override
    public void initialize() {
        log.info("Intitializing");
        prepareToDriveAtGivenNoteWithWaypoints(getWaypoints());
    }

    public void prepareToDriveAtGivenNote() {
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                drive.getTargetNote(), 10));
        // when driving to a note, the robot must face backwards, as the robot's intake is on the back
        this.logic.setKeyPoints(swervePoints);
        this.logic.setAimAtGoalDuringFinalLeg(true);
        this.logic.setDriveBackwards(true);
        this.logic.setEnableConstantVelocity(true);
        this.logic.setConstantVelocity(drive.getSuggestedAutonomousMaximumSpeed());
        // this is commented out because we want our autonomous to be very basic right now
//        this.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
        reset();
    }
    //allows for driving not in a straight line
    public void prepareToDriveAtGivenNoteWithWaypoints(Translation2d... waypoints){
        if (waypoints == null){
            prepareToDriveAtGivenNote();
            return;
        }
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        for (Translation2d waypoint : waypoints){
            swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(waypoint,Rotation2d.fromDegrees(180),10));
        }
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                drive.getTargetNote(), 10));
        // when driving to a note, the robot must face backwards, as the robot's intake is on the back
        this.logic.setKeyPoints(swervePoints);
        this.logic.setAimAtGoalDuringFinalLeg(true);
        this.logic.setDriveBackwards(true);
        this.logic.setEnableConstantVelocity(true);
        this.logic.setConstantVelocity(drive.getSuggestedAutonomousMaximumSpeed());
        // this is commented out because we want our autonomous to be very basic right now
//        this.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
        reset();
    }
    public void setWaypoints(Translation2d... waypoints){
        this.waypoints = waypoints;
    }

    public Translation2d[] getWaypoints() {
        return waypoints;
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
