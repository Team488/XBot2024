package competition.commandgroups;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.NoteAcquisitionMode;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import org.kobe.xbot.ClientLite.XTablesClient;

import javax.inject.Inject;
import java.util.ArrayList;

public class DriveToWaypointsWithVisionCommand extends SwerveSimpleTrajectoryCommand {

    DriveSubsystem drive;
    public Translation2d[] waypoints = null;
    double maximumSpeedOverride = 0;
    PoseSubsystem pose;
    VisionSubsystem vision;
    CollectorSubsystem collector;
    boolean hasDoneVisionCheckYet = false;
    XTablesClient xclient;

    private NoteAcquisitionMode noteAcquisitionMode = NoteAcquisitionMode.BlindApproach;

    @Inject
    DriveToWaypointsWithVisionCommand(PoseSubsystem pose, DriveSubsystem drive,
                                      PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                      VisionSubsystem vision, CollectorSubsystem collector) {
        super(drive, pose, pf, headingModuleFactory);
        this.pose = pose;
        this.drive = drive;
        this.vision = vision;
        this.collector = collector;
    }

    @Override
    public void initialize() {
        noteAcquisitionMode = NoteAcquisitionMode.BlindApproach;
        hasDoneVisionCheckYet = false;
        // The init here takes care of going to the initially given "static" note position.
        super.initialize();
    }

    //allows for driving not in a straight line
    public void prepareToDriveAtGivenNoteWithWaypoints(Translation2d... waypoints){
        if (waypoints == null){
            return;
        }
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        for (Translation2d waypoint : waypoints){
            swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(waypoint, Rotation2d.fromDegrees(180),5));
        }
        // when driving to a note, the robot must face backwards, as the robot's intake is on the back
        this.logic.setKeyPoints(swervePoints);
        this.logic.setAimAtGoalDuringFinalLeg(true);
        this.logic.setDriveBackwards(true);
        this.logic.setEnableConstantVelocity(true);

        double suggestedSpeed = drive.getSuggestedAutonomousMaximumSpeed();
        if (maximumSpeedOverride > suggestedSpeed) {
            log.info("Using maximum speed override");
            suggestedSpeed = maximumSpeedOverride;
        } else {
            log.info("Not using max speed override");
        }

        this.logic.setConstantVelocity(suggestedSpeed);

        reset();
    }

    //allows for driving not in a straight line
    public void retrieveWaypointsFromVision() {
        if (this.xclient == null) {
            this.xclient = new XTablesClient(1735, false, 5, false);
        }

        ArrayList<Coordinate> coordinates = this.xclient.getArray("target_waypoints", Coordinate.class).complete();
        ArrayList<Translation2d> waypoints = new ArrayList<Translation2d>();
        for (Coordinate coordinate : coordinates) {
            waypoints.add(new Translation2d(coordinate.x, coordinate.y));
        }

        this.prepareToDriveAtGivenNoteWithWaypoints(waypoints.toArray(new Translation2d[waypoints.size()]));
    }

    @Override
    public void execute() {
        this.retrieveWaypointsFromVision();
        super.execute();
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }

    private class Coordinate {
        double x;
        double y;
    }
}
