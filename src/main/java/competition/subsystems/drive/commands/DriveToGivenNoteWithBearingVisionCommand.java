package competition.subsystems.drive.commands;

import competition.commandgroups.DriveToGivenNoteWithVisionCommand;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.controls.sensors.XTimer;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.function.Supplier;

public class DriveToGivenNoteWithBearingVisionCommand extends DriveToGivenNoteCommand {

    DynamicOracle oracle;
    PoseSubsystem pose;
    DriveSubsystem drive;
    VisionSubsystem vision;
    CollectorSubsystem collector;
    boolean hasDoneVisionCheckYet = false;

    public enum NoteAcquisitionMode {
        BlindApproach,
        VisionApproach,
        VisionTerminalApproach,
        BackAwayToTryAgain,
        GiveUp
    }

    protected NoteAcquisitionMode noteAcquisitionMode = NoteAcquisitionMode.BlindApproach;
    double frozenHeading = 0;

    // For now, we will be using time to determine when to change vision modes.
    protected double timeWhenVisionModeEntered = Double.MAX_VALUE;
    double visionModeDuration = 0.5;
    protected double timeWhenTerminalVisionModeEntered = Double.MAX_VALUE;
    double terminalVisionModeDuration = 0.3;

    @Inject
    DriveToGivenNoteWithBearingVisionCommand(PoseSubsystem pose, DriveSubsystem drive, DynamicOracle oracle,
                                             PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                             VisionSubsystem vision, CollectorSubsystem collector) {
        super(drive, oracle, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.pose = pose;
        this.drive = drive;
        this.vision = vision;
        this.collector = collector;
    }

    @Override
    public void initialize() {
        // The init here takes care of going to the initially given "static" note position.
        super.initialize();
        noteAcquisitionMode = NoteAcquisitionMode.BlindApproach;
        hasDoneVisionCheckYet = false;
        resetVisionModeTimers();
    }

    private void resetVisionModeTimers() {
        timeWhenVisionModeEntered = Double.MAX_VALUE;
        timeWhenTerminalVisionModeEntered = Double.MAX_VALUE;
    }

    @Override
    public void execute() {

        // Check for mode changes
        switch (noteAcquisitionMode) {
            case BlindApproach:
                if (!hasDoneVisionCheckYet) {
                    double rangeToStaticNote = pose.getCurrentPose2d().getTranslation().getDistance(
                            drive.getTargetNote().getTranslation());
                    aKitLog.record("RangeToStaticNote", rangeToStaticNote);
                    if (rangeToStaticNote < vision.getBestRangeFromStaticNoteToSearchForNote()) {
                        hasDoneVisionCheckYet = true;
                        log.info("Close to static note - attempting vision update.");
                        if (vision.getCenterCamLargestNoteTarget().isPresent()) {
                            log.info("Found with central camera. Advancing using vision");
                            noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
                            timeWhenVisionModeEntered = XTimer.getFPGATimestamp();
                        } else {
                            log.info("No note found with central camera. Staying in blind approach.");
                        }
                    }
                }
                break;
            case VisionApproach:
                if (shouldEnterTerminalVisionApproach()) {
                    log.info("Switching to terminal vision approach");
                    noteAcquisitionMode = NoteAcquisitionMode.VisionTerminalApproach;
                    timeWhenTerminalVisionModeEntered = XTimer.getFPGATimestamp();
                    frozenHeading = pose.getCurrentHeading().getDegrees();
                }
                break;
            case VisionTerminalApproach:
                if (shouldExitTerminalVisionApproach()) {
                    log.info("Switching to back away to try again");
                    noteAcquisitionMode = NoteAcquisitionMode.BackAwayToTryAgain;
                    setBackingAwayFromNoteTarget();
                }
                break;
            case BackAwayToTryAgain:
                if (super.isFinished()) {
                    // check to see if we see a note. If not, give up.
                    if (vision.getCenterCamLargestNoteTarget().isPresent()) {
                        log.info("Found a note. Switching to vision mode.");
                        resetVisionModeTimers();
                        timeWhenVisionModeEntered = XTimer.getFPGATimestamp();
                        noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
                    } else {
                        log.info("Can't see a note. Giving up.");
                        noteAcquisitionMode = NoteAcquisitionMode.GiveUp;
                    }
                }
                break;
            case GiveUp:
                // Do nothing. Command will exit momentarily.
                break;
            default:
                log.info("Unknown mode: " + noteAcquisitionMode);
                noteAcquisitionMode = NoteAcquisitionMode.GiveUp;
                break;
        }


        // Then perform actions.

        // If we are doing field-relative driving, then we need to use the
        // underlying SwerveSimpleTrajectoryCommand to go to specific points
        if (noteAcquisitionMode == NoteAcquisitionMode.BlindApproach
        || noteAcquisitionMode == NoteAcquisitionMode.BackAwayToTryAgain) {
            super.execute();
        }

        double approachPower =
                -drive.getSuggestedAutonomousMaximumSpeed() / drive.getMaxTargetSpeedMetersPerSecond();
        double terminalPower = approachPower * 0.5;
        // If we are doing vision stuff, then we need to use robot-relative driving.
        // When approaching dynamically, drive pretty fast and keep pointing at the note.
        if (noteAcquisitionMode == NoteAcquisitionMode.VisionApproach) {
            var target = vision.getCenterCamLargestNoteTarget();
            if (target.isPresent()) {
                double rotationPower =
                        2*this.drive.getRotateToHeadingPid().calculate(0, target.get().getYaw());

                drive.move(new XYPair(approachPower, 0), rotationPower);
            }
        }
        // Slow down a bit if in terminal approach
        if (noteAcquisitionMode == NoteAcquisitionMode.VisionTerminalApproach) {
            double rotationPower = headingModule.calculateHeadingPower(frozenHeading);
            drive.move(new XYPair(terminalPower, 0), rotationPower);
        }

    }

    private boolean shouldEnterTerminalVisionApproach() {
        if (XTimer.getFPGATimestamp() > timeWhenVisionModeEntered + visionModeDuration) {
            return true;
        }
        return false;
    }

    private boolean shouldExitTerminalVisionApproach() {
        if (XTimer.getFPGATimestamp() > timeWhenTerminalVisionModeEntered + terminalVisionModeDuration) {
            return true;
        }
        return false;
    }

    private void setBackingAwayFromNoteTarget() {
        var newTarget = pose.transformRobotCoordinateToFieldCoordinate(new Translation2d(1,0));

        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(new XbotSwervePoint(newTarget, pose.getCurrentHeading(), 10));
        // when driving to a note, the robot must face backwards, as the robot's intake is on the back
        this.logic.setKeyPoints(swervePoints);
        this.logic.setAimAtGoalDuringFinalLeg(false);
        this.logic.setDriveBackwards(false);
        this.logic.setEnableConstantVelocity(true);
        reset();
    }

    @Override
    public boolean isFinished() {
        return collector.confidentlyHasControlOfNote() || noteAcquisitionMode == NoteAcquisitionMode.GiveUp;
    }
}
