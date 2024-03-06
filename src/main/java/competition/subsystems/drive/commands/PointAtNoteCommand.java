package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class PointAtNoteCommand extends BaseCommand {
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
        // Find the note we want to point at
        var notePosition = this.oracle.getNoteMap().getClosestAvailableNote(this.pose.getCurrentPose2d());
        if (notePosition != null) {
            this.notePosition = notePosition.toPose2d();
        } else {
            this.notePosition = null;
        }
    }

    @Override
    public void execute() {
        if (notePosition == null) {
            return;
        }

        double rotationError = pose.getAngularErrorToTranslation2dInDegrees(notePosition.getTranslation());
        double rotationPower = this.drive.getRotateToHeadingPid().calculate(0, rotationError);

        if (drive.isRobotOrientedDriveActive()) {
            drive.move(new XYPair(0,0), rotationPower);
        } else {
            drive.fieldOrientedDrive(new XYPair(0,0), rotationPower, pose.getCurrentHeading().getDegrees(), new XYPair(0,0));
        }
    }

    @Override
    public boolean isFinished() {
        if (this.notePosition == null) {
            return true;
        }

        return this.drive.getRotateToHeadingPid().isOnTarget();
    }
}
