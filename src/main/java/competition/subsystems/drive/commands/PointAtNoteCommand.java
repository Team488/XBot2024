package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class PointAtNoteCommand extends BaseCommand {
    Logger log = LogManager.getLogger(this);

    Pose2d savedNotePosition = null;
    DriveSubsystem drive;
    HeadingModule headingModule;
    PoseSubsystem pose;
    OperatorInterface oi;
    DynamicOracle oracle;
    DoubleProperty maxNoteJump;

    @Inject
    public PointAtNoteCommand(DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PoseSubsystem pose,
                              OperatorInterface oi, DynamicOracle oracle, PropertyFactory pf) {
        this.drive = drive;
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.pose = pose;
        this.oi = oi;
        this.oracle = oracle;

        pf.setPrefix(this);
        this.maxNoteJump = pf.createPersistentProperty("Maximum position jump meters", 1.0);

        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        savedNotePosition = null;
        // Find the note we want to point at
        var notePosition = getClosestAvailableNote();
        if (notePosition != null) {
            this.savedNotePosition = notePosition;
            log.info("Rotating to note at [{}, {}], current rotation error: {}",
                    notePosition.getX(), notePosition.getY(), getRotationError());
        } else {
            this.savedNotePosition = null;
            log.warn("No note found to rotate to");
        }
    }

    @Override
    public void execute() {
        if (savedNotePosition == null) {
            log.warn("Skipping execute due to no target.");
            return;
        }

        // If we can still see the note, update the target
        var newTarget = getClosestAvailableNote();
        if (newTarget != null && newTarget != this.savedNotePosition) {
            this.savedNotePosition = newTarget;
        }

        var toNoteTranslation = newTarget.getTranslation().minus(this.pose.getCurrentPose2d().getTranslation());
        var movement = MathUtils.deadband(getDriveIntent(toNoteTranslation, oi.driverGamepad.getLeftVector()), oi.getDriverGamepadTypicalDeadband(), (x) -> x);

        double rotationPower = this.drive.getRotateToHeadingPid().calculate(0, getRotationError());

        drive.move(new XYPair(movement, 0), rotationPower);
    }

    public static double getDriveIntent(Translation2d fieldTranslationToTarget, XYPair driveJoystick) {
        var toNoteVector = fieldTranslationToTarget.toVector().unit();
        var driverVector = VecBuilder.fill(driveJoystick.x, -driveJoystick.y);
        var dot = toNoteVector.dot(driverVector);

        return dot;
    }

    @Override
    public boolean isFinished() {
        if (this.savedNotePosition == null) {
            log.warn("Command finished due to no note.");
            return true;
        }
        return false;
    }

    private Pose2d getClosestAvailableNote() {
        var virtualPoint = getProjectedPoint();
        var notePosition = this.oracle.getNoteMap().getClosestAvailableNote(virtualPoint, false);

        if (notePosition != null) {
            if (this.savedNotePosition == null) {
                this.savedNotePosition = notePosition.toPose2d();
            };
            var distance = this.savedNotePosition
                    .getTranslation()
                    .getDistance(notePosition.toPose2d().getTranslation());
            if (distance < this.maxNoteJump.get() && distance > 0.05) {
                log.info("Updating target");
                return notePosition.toPose2d();
            }
        }

        return this.savedNotePosition;
    }

    private Pose2d getProjectedPoint() {
        return this.pose.getCurrentPose2d().plus(new Transform2d(-0.4, 0, new Rotation2d()));
    }

    private double getRotationError() {
        return this.pose.getAngularErrorToTranslation2dInDegrees(
                this.savedNotePosition.getTranslation(),
                Rotation2d.fromDegrees(180)); // point rear of robot
    }
}
