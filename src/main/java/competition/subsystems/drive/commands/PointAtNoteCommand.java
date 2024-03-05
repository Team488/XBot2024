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

        double rotateIntent = getSuggestedRotateIntent();

        if (drive.isRobotOrientedDriveActive()) {
            drive.move(new XYPair(0,0), rotateIntent);
        } else {
            drive.fieldOrientedDrive(new XYPair(0,0), rotateIntent, pose.getCurrentHeading().getDegrees(), new XYPair(0,0));
        }
    }

    private double getRotationIntentPointAtNote(Pose2d currentPose) {
        Translation2d currentXY = new Translation2d(currentPose.getX(), currentPose.getY());

        return currentXY.minus(notePosition.getTranslation()).getAngle().getDegrees() + 180;
    }

    @Override
    public boolean isFinished() {
        if (this.notePosition == null) {
            return true;
        }
        return false;
    }

    private double getSuggestedRotateIntent() {
        double suggestedRotatePower;
        // If we are using absolute orientation, we first need get the desired heading from the right joystick.
        // We need to only do this if the joystick has been moved past the minimumMagnitudeForAbsoluteHeading.
        // In the future, we might be able to replace the joystick with a dial or other device that can more easily
        // hold a heading.

        double desiredHeading = 0;
        desiredHeading = getRotationIntentPointAtNote(pose.getCurrentPose2d());

        drive.setDesiredHeading(desiredHeading);

        suggestedRotatePower = headingModule.calculateHeadingPower(desiredHeading);

        return suggestedRotatePower;
    }
}
