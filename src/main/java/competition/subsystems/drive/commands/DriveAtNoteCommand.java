package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.logic.TimeStableValidator;
import xbot.common.math.XYPair;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class DriveAtNoteCommand extends BaseCommand {

    DriveSubsystem drive;
    VisionSubsystem vision;
    CollectorSubsystem collector;
    OperatorInterface oi;
    public enum DriveAtNoteStates {
        Searching,
        RotateWhileApproaching,
        CommitToApproach
    }

    double startRotatingTime = Double.MAX_VALUE;
    double rotatingDuration = 0.5;
    double startFinalApproachTime = Double.MAX_VALUE;
    double finalApproachDuration = 2.0;

    private HeadingModule headingModule;

    private DriveAtNoteStates state = DriveAtNoteStates.Searching;

    @Inject
    public DriveAtNoteCommand(DriveSubsystem drive, VisionSubsystem vision, CollectorSubsystem collector,
                              OperatorInterface oi, HeadingModule.HeadingModuleFactory headingModuleFactory) {
        this.drive = drive;
        this.vision = vision;
        this.collector = collector;
        this.addRequirements(drive);
        headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        state = DriveAtNoteStates.Searching;
        startRotatingTime = Double.MAX_VALUE;
        startFinalApproachTime = Double.MAX_VALUE;
        headingModule.reset();
    }

    @Override
    public void execute() {

        double reportedYawToNote = vision.getNoteYawFromCentralCamera();
        boolean isYawValid = Math.abs(reportedYawToNote) > 0.001;

        // Check for state changes
        switch (state) {
            case Searching:
                if (isYawValid) {
                    state = DriveAtNoteStates.RotateWhileApproaching;
                    startRotatingTime = XTimer.getFPGATimestamp();
                }
                break;
            case RotateWhileApproaching:
                if (XTimer.getFPGATimestamp() > startRotatingTime + rotatingDuration) {
                    state = DriveAtNoteStates.CommitToApproach;
                    startFinalApproachTime = XTimer.getFPGATimestamp();

                }
                break;
            default:
                // no op.
                break;
        }

        double drivePower = 0;
        double rotatePower = 0;
        double typicalApproachPower = -0.25;

        switch (state) {
            case Searching:
                // don't drive anywhere.
                break;
            case RotateWhileApproaching:
                drivePower = -typicalApproachPower;
                rotatePower = headingModule.calculateDeltaHeadingPower(reportedYawToNote);
                break;
            case CommitToApproach:
                drivePower = -typicalApproachPower;
                rotatePower = headingModule.calculateFrozenHeadingPower();
                break;
            default:
                // no op.
                break;
        }

        // Robot-relative driving
        drive.move(new XYPair(drivePower, 0), rotatePower);

        aKitLog.record("DriveAtNoteState", state.toString());
        aKitLog.record("DriveAtNoteYaw", reportedYawToNote);
    }

    @Override
    public boolean isFinished() {
        return collector.confidentlyHasControlOfNote() || XTimer.getFPGATimestamp() > startFinalApproachTime + finalApproachDuration;
    }
}
