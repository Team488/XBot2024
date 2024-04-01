package competition.subsystems.vision;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import org.apache.logging.log4j.LogManager;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Comprises reusable logic to discover, track, and acquire notes.
 */
public class NoteSeekLogic {

    VisionSubsystem vision;
    DynamicOracle oracle;
    PoseSubsystem pose;
    DriveSubsystem drive;

    NoteAcquisitionMode initialMode = NoteAcquisitionMode.BlindApproach;
    NoteAcquisitionMode noteAcquisitionMode = NoteAcquisitionMode.BlindApproach;

    double frozenHeading = 0;
    double timeWhenVisionModeEntered = Double.MAX_VALUE;
    double timeWhenTerminalVisionModeEntered = Double.MAX_VALUE;
    double timeWhenRotationSearchModeEntered = Double.MAX_VALUE;
    double timeWhenBackUpModeEntered = Double.MAX_VALUE;

    final DoubleProperty terminalVisionModeDuration;
    final DoubleProperty rotationSearchDuration;
    final DoubleProperty rotationSearchPower;
    final DoubleProperty terminalVisionModePowerFactor;
    final DoubleProperty backUpDuration;

    boolean hasDoneVisionCheckYet = false;
    protected final AKitLogger aKitLog;
    String akitPrefix = "NoteSeekLogic/";
    org.apache.logging.log4j.Logger log = LogManager.getLogger(this.getClass());

    private Pose2d suggestedLocation;
    private Twist2d suggestedPowers;

    private final HeadingModule headingModule;
    private boolean allowRotationSearch = false;

    @Inject
    public NoteSeekLogic(VisionSubsystem vision, DynamicOracle oracle, PoseSubsystem pose,
                         DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PropertyFactory pf) {
        this.vision = vision;
        this.oracle = oracle;
        this.pose = pose;
        this.drive = drive;

        pf.setPrefix("NoteSeekLogic");
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        terminalVisionModeDuration = pf.createPersistentProperty("TerminalVisionModeDuration", 1.0);
        rotationSearchDuration = pf.createPersistentProperty("RotationSearchDuration", 3.0);
        rotationSearchPower = pf.createPersistentProperty("RotationSearchPower", 0.5);
        terminalVisionModePowerFactor = pf.createPersistentProperty("TerminalVisionModePowerFactor", 0.5);
        backUpDuration = pf.createPersistentProperty("BackUpDuration", 1.0);

        headingModule = headingModuleFactory.create(drive.getAggressiveGoalHeadingPid());

        aKitLog = new AKitLogger(akitPrefix);
    }

    public void setInitialMode(NoteAcquisitionMode mode) {
        initialMode = mode;
    }

    public void setAllowRotationSearch(boolean allowRotationSearch) {
        this.allowRotationSearch = allowRotationSearch;
    }

    public void reset() {
        noteAcquisitionMode = initialMode;
        resetVisionModeTimers();
        if (initialMode == NoteAcquisitionMode.VisionApproach) {
            timeWhenVisionModeEntered = XTimer.getFPGATimestamp();
        }
        hasDoneVisionCheckYet = false;
    }

    private void resetVisionModeTimers() {
        timeWhenVisionModeEntered = Double.MAX_VALUE;
        timeWhenTerminalVisionModeEntered = Double.MAX_VALUE;
        timeWhenRotationSearchModeEntered = Double.MAX_VALUE;
        timeWhenBackUpModeEntered = Double.MAX_VALUE;
    }

    private void checkForModeChanges(boolean atTargetPose) {
        switch (noteAcquisitionMode) {
            case BlindApproach:
                if (!hasDoneVisionCheckYet) {

                    if (drive.getTargetNote() == null) {
                        log.info("No target note set.");
                        if (allowRotationSearch) {
                            log.info("Attempting to find one via rotation.");
                            noteAcquisitionMode = NoteAcquisitionMode.SearchViaRotation;
                            timeWhenRotationSearchModeEntered = XTimer.getFPGATimestamp();
                        } else {
                            log.info("Giving up.");
                            noteAcquisitionMode = NoteAcquisitionMode.GiveUp;
                        }
                        break;
                    }

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
                    timeWhenBackUpModeEntered = XTimer.getFPGATimestamp();
                    noteAcquisitionMode = NoteAcquisitionMode.BackAwayToTryAgain;
                    suggestedLocation = new Pose2d(
                            pose.transformRobotCoordinateToFieldCoordinate(new Translation2d(1,0)),
                            pose.getCurrentHeading());
                }
                break;
            case BackAwayToTryAgain:
                if (shouldExitBackUp(atTargetPose)) {
                    // check to see if we see a note. If not, give up or search.
                    if (vision.getCenterCamLargestNoteTarget().isPresent()) {
                        log.info("Found a note. Switching to vision mode.");
                        resetVisionModeTimers();
                        timeWhenVisionModeEntered = XTimer.getFPGATimestamp();
                        noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
                    } else {
                        log.info("Can't see a note.");
                        if (allowRotationSearch) {
                            log.info("Attempting to find one via rotation.");
                            noteAcquisitionMode = NoteAcquisitionMode.SearchViaRotation;
                            timeWhenRotationSearchModeEntered = XTimer.getFPGATimestamp();
                        } else {
                            log.info("Giving up.");
                            noteAcquisitionMode = NoteAcquisitionMode.GiveUp;
                        }
                    }
                }
                break;
            case SearchViaRotation:
                if (vision.getCenterCamLargestNoteTarget().isPresent()) {
                    log.info("Found a note. Switching to vision mode.");
                    resetVisionModeTimers();
                    timeWhenVisionModeEntered = XTimer.getFPGATimestamp();
                    noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
                } else if (shouldExitRotationSearch()) {
                    log.info("Giving up.");
                    noteAcquisitionMode = NoteAcquisitionMode.GiveUp;
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
        aKitLog.record("NoteAcquisitionMode", noteAcquisitionMode);
    }

    public NoteSeekAdvice getAdvice(boolean atTargetPose) {
        checkForModeChanges(atTargetPose);

        double approachPower =
                -drive.getSuggestedAutonomousMaximumSpeed() / drive.getMaxTargetSpeedMetersPerSecond();
        double terminalPower = approachPower * terminalVisionModePowerFactor.get();

        switch (noteAcquisitionMode) {
            case BlindApproach:
                // Only return the state - calling class will figure out how to approach.
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.empty());
            case VisionApproach:
                var target = vision.getCenterCamLargestNoteTarget();
                if (target.isPresent()) {
                    double rotationPower =
                            this.drive.getAggressiveGoalHeadingPid().calculate(0, target.get().getYaw());
                    suggestedPowers = new Twist2d(approachPower, 0, rotationPower);
                }
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.of(suggestedPowers));
            case VisionTerminalApproach:
                double rotationPower = headingModule.calculateHeadingPower(frozenHeading);
                suggestedPowers = new Twist2d(terminalPower, 0, rotationPower);
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.of(suggestedPowers));
            case BackAwayToTryAgain:
                return new NoteSeekAdvice(
                        noteAcquisitionMode, Optional.of(suggestedLocation),Optional.empty());
            case SearchViaRotation:
                suggestedPowers = new Twist2d(0, 0, rotationSearchPower.get());
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.of(suggestedPowers));
            case GiveUp:
            default:
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.empty());
        }
    }
    private boolean shouldEnterTerminalVisionApproach() {
        var target = vision.getCenterCamLargestNoteTarget();

        boolean lostTarget = target.isEmpty();
        if (lostTarget) {
            return true;
        }

        boolean atOrBelowTerminalPitch = target.get().getPitch() < vision.terminalNotePitch;
        boolean roughlyCentered = Math.abs(target.get().getYaw()) < vision.getTerminalNoteYawRange();

        return atOrBelowTerminalPitch && roughlyCentered;
    }

    private boolean shouldExitTerminalVisionApproach() {
        if (XTimer.getFPGATimestamp() > timeWhenTerminalVisionModeEntered + terminalVisionModeDuration.get()) {
            return true;
        }
        return false;
    }

    private boolean shouldExitRotationSearch() {
        if (XTimer.getFPGATimestamp() > timeWhenRotationSearchModeEntered + rotationSearchDuration.get()) {
            return true;
        }
        return false;
    }

    private boolean shouldExitBackUp(boolean atTargetPosition) {
        return atTargetPosition
                || XTimer.getFPGATimestamp() > timeWhenBackUpModeEntered + backUpDuration.get()
                || vision.getCenterCamLargestNoteTarget().isPresent();
    }


}
