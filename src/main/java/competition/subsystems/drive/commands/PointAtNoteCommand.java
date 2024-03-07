package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class PointAtNoteCommand extends BaseCommand {
    Logger log = LogManager.getLogger(this);

    Pose2d notePosition;
    DriveSubsystem drive;
    HeadingModule headingModule;
    PoseSubsystem pose;
    OperatorInterface oi;
    DynamicOracle oracle;

    @Inject
    public PointAtNoteCommand(DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PoseSubsystem pose,
                              OperatorInterface oi, DynamicOracle oracle, PropertyFactory pf) {
        this.drive = drive;
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.pose = pose;
        this.oi = oi;
        this.oracle = oracle;

        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        // Find the note we want to point at
        // Project a point in front of the robot's collector to bias preferred notes in that direction
        var virtualPoint = this.pose.getCurrentPose2d().plus(new Transform2d(-0.4, 0, new Rotation2d()));
        var notePosition = this.oracle.getNoteMap().getClosestAvailableNote(virtualPoint);
        if (notePosition != null) {
            this.notePosition = notePosition.toPose2d();
            log.info("Rotating to note");
        } else {
            this.notePosition = null;
            log.warn("No note found to rotate to");
        }
    }

    @Override
    public void execute() {
        if (notePosition == null) {
            log.warn("Skipping execute due to no target.");
            return;
        }

        var movement = -oi.driverGamepad.getLeftStickY();

        double rotationError = this.pose.getAngularErrorToTranslation2dInDegrees(
                notePosition.getTranslation(),
                Rotation2d.fromDegrees(180)); // point rear of robot
        double rotationPower = this.drive.getRotateToHeadingPid().calculate(0, rotationError);

        drive.move(new XYPair(movement, 0), rotationPower);
    }

    @Override
    public boolean isFinished() {
        if (this.notePosition == null) {
            log.warn("Command finished due to no note.");
            return true;
        }
        return false;
        //var isOnTarget =  this.drive.getRotateToHeadingPid().isOnTarget();
        //if (isOnTarget) {
        //    log.info("Finished");
       // }
        //return isOnTarget;
    }
}
