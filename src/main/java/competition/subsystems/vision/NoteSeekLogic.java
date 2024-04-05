package competition.subsystems.vision;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
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
    Translation2d frozenNoteTarget;

    final TimeoutTracker visionModeTimeoutTracker;
    final TimeoutTracker terminalVisionModeTimeoutTracker;
    final TimeoutTracker rotationSearchModeTimeoutTracker;
    final TimeoutTracker backupModeTimeoutTracker;
    final TimeoutTracker rotateToNoteModeTimeoutTracker;

    final DoubleProperty visionModeDuration;

    final DoubleProperty terminalVisionModeDuration;
    final DoubleProperty rotationSearchDuration;
    final DoubleProperty rotationSearchPower;
    final DoubleProperty terminalVisionModePowerFactor;
    final DoubleProperty backUpDuration;
    final DoubleProperty rotateToNoteDuration;

    boolean hasDoneVisionCheckYet = false;
    protected final AKitLogger aKitLog;
    String akitPrefix = "NoteSeekLogic/";
    org.apache.logging.log4j.Logger log = LogManager.getLogger(this.getClass());

    private Pose2d suggestedLocation;
    private Twist2d suggestedPowers;

    private final HeadingModule headingModule;
    private boolean allowRotationSearch = false;
    private VisionRange visionRange = VisionRange.Close;

    @Inject
    public NoteSeekLogic(VisionSubsystem vision, DynamicOracle oracle, PoseSubsystem pose,
                         DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PropertyFactory pf) {
        this.vision = vision;
        this.oracle = oracle;
        this.pose = pose;
        this.drive = drive;

        pf.setPrefix("NoteSeekLogic");
        pf.setDefaultLevel(Property.PropertyLevel.Important);

        visionModeDuration = pf.createPersistentProperty("VisionModeDuration", 5);
        terminalVisionModeDuration = pf.createPersistentProperty("TerminalVisionModeDuration", 1.0);
        rotationSearchDuration = pf.createPersistentProperty("RotationSearchDuration", 4.5);
        rotationSearchPower = pf.createPersistentProperty("RotationSearchPower", 0.17);
        terminalVisionModePowerFactor = pf.createPersistentProperty("TerminalVisionModePowerFactor", 0.5);
        backUpDuration = pf.createPersistentProperty("BackUpDuration", 1.0);
        rotateToNoteDuration = pf.createPersistentProperty("RotateToNoteDuration", 1.0);

        visionModeTimeoutTracker = new TimeoutTracker(() -> visionModeDuration.get());
        rotateToNoteModeTimeoutTracker = new TimeoutTracker(() -> rotateToNoteDuration.get());
        terminalVisionModeTimeoutTracker = new TimeoutTracker(() -> terminalVisionModeDuration.get());
        rotationSearchModeTimeoutTracker = new TimeoutTracker(() -> rotationSearchDuration.get());
        backupModeTimeoutTracker = new TimeoutTracker(() -> backUpDuration.get());
        
        headingModule = headingModuleFactory.create(drive.getAggressiveGoalHeadingPid());

        aKitLog = new AKitLogger(akitPrefix);
    }

    public void setInitialMode(NoteAcquisitionMode mode) {
        initialMode = mode;
    }

    public void setAllowRotationSearch(boolean allowRotationSearch) {
        this.allowRotationSearch = allowRotationSearch;
    }

    public void setVisionRange(VisionRange range) {
        visionRange = range;
    }

    public void reset() {
        noteAcquisitionMode = initialMode;
        resetVisionModeTimers();
        if (initialMode == NoteAcquisitionMode.CenterCameraVisionApproach) {
            visionModeTimeoutTracker.start();
        }
        hasDoneVisionCheckYet = false;
    }

    private void resetVisionModeTimers() {
        visionModeTimeoutTracker.reset();
        terminalVisionModeTimeoutTracker.reset();
        rotationSearchModeTimeoutTracker.reset();
        backupModeTimeoutTracker.reset();
        rotateToNoteModeTimeoutTracker.reset();
    }

    private void checkForModeChanges(boolean atTargetPose) {
        switch (noteAcquisitionMode) {
            case BlindApproach:
                // If no note has been set, then we either need to give up
                // or try searching via rotation or other methods.
                if (drive.getTargetNote() == null) {
                    log.info("No target note set.");
                    decideWhetherToGiveUpOrRotate();
                    break;
                }

                double rangeToStaticNote = pose.getCurrentPose2d().getTranslation().getDistance(
                        drive.getTargetNote().getTranslation());
                aKitLog.record("RangeToStaticNote", rangeToStaticNote);

                if (rangeToStaticNote < vision.getBestRangeFromStaticNoteToSearchForNote()) {
                    if (!hasDoneVisionCheckYet) {
                        log.info("Close to static note - attempting vision update.");
                        hasDoneVisionCheckYet = true;
                    }
                    evaluateIfShouldMoveToVisionBasedCollection(false);
                }
                break;
            case RotateToNoteDetectedByCornerCameras:
                // Rotate until either the center camera sees the note nice and solidly or we have been in this
                // mode too long.
                if (hasSolidLockOnNoteWithCenterCamera()) {
                    noteAcquisitionMode = NoteAcquisitionMode.CenterCameraVisionApproach;
                    visionModeTimeoutTracker.start();
                }
                if (rotateToNoteModeTimeoutTracker.getTimedOut()) {
                    decideWhetherToGiveUpOrRotate();
                }
                break;
            case CenterCameraVisionApproach:
                if (shouldEnterTerminalVisionApproach()) {
                    log.info("Switching to terminal vision approach");
                    noteAcquisitionMode = NoteAcquisitionMode.CenterCameraTerminalApproach;
                    terminalVisionModeTimeoutTracker.start();
                    frozenHeading = pose.getCurrentHeading().getDegrees();
                }
                break;
            case CenterCameraTerminalApproach:
                if (shouldExitTerminalVisionApproach()) {
                    log.info("Going to rotation search");
                    rotationSearchModeTimeoutTracker.start();
                    noteAcquisitionMode = NoteAcquisitionMode.SearchViaRotation;
                }
                break;
            case BackAwayToTryAgain:
                if (shouldExitBackUp(atTargetPose)) {
                    // check to see if we see a note. If not, give up or search.
                    if (vision.getCenterCamLargestNoteTarget().isPresent()) {
                        log.info("Found a note. Switching to vision mode.");
                        resetVisionModeTimers();
                        visionModeTimeoutTracker.start();
                        noteAcquisitionMode = NoteAcquisitionMode.CenterCameraVisionApproach;
                    } else {
                        log.info("Can't see a note.");
                        decideWhetherToGiveUpOrRotate();
                    }
                }
                break;
            case SearchViaRotation:
                evaluateIfShouldMoveToVisionBasedCollection(true);
                if (shouldExitRotationSearch()) {
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

    private void evaluateIfShouldMoveToVisionBasedCollection(boolean resetTimersIfFound) {
        var scannedNote = scanForNote();
        if (scannedNote.isPresent()) {
            if (scannedNote.get().getSource() == NoteDetectionSource.CenterCamera) {
                log.info("Found with central camera. Advancing using vision");
                noteAcquisitionMode = NoteAcquisitionMode.CenterCameraVisionApproach;
                visionModeTimeoutTracker.start();
            } else {
                log.info("Found with a corner camera. Advancing using vision");
                noteAcquisitionMode = NoteAcquisitionMode.RotateToNoteDetectedByCornerCameras;
                frozenHeading = scannedNote.get().getNote().yaw;
                rotateToNoteModeTimeoutTracker.start();
            }
            if (resetTimersIfFound) {
                resetVisionModeTimers();
            }
        }
    }

    public NoteSeekAdvice getAdvice(boolean atTargetPose) {
        checkForModeChanges(atTargetPose);

        double approachPower =
                -drive.getSuggestedAutonomousMaximumSpeed() / drive.getMaxTargetSpeedMetersPerSecond();
        double terminalPower = approachPower * terminalVisionModePowerFactor.get();
        double rotationPower = 0;
        switch (noteAcquisitionMode) {
            case BlindApproach:
                // Only return the state - calling class will figure out how to approach.
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.empty());
            case RotateToNoteDetectedByCornerCameras:
                rotationPower = headingModule.calculateHeadingPower(frozenHeading);
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.of(new Twist2d(0, 0, rotationPower)));
            case CenterCameraVisionApproach:
                var target = vision.getCenterCamLargestNoteTarget();
                if (target.isPresent()) {
                    rotationPower = this.drive.getAggressiveGoalHeadingPid().calculate(0, target.get().getYaw());
                    suggestedPowers = new Twist2d(approachPower, 0, rotationPower);
                }
                return new NoteSeekAdvice(noteAcquisitionMode, Optional.empty(), Optional.of(suggestedPowers));
            case CenterCameraTerminalApproach:
                rotationPower = headingModule.calculateHeadingPower(frozenHeading);
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
        return terminalVisionModeTimeoutTracker.getTimedOut();
    }

    private boolean shouldExitRotationSearch() {
        return rotationSearchModeTimeoutTracker.getTimedOut();
    }

    private boolean shouldExitBackUp(boolean atTargetPosition) {
        return atTargetPosition
                || backupModeTimeoutTracker.getTimedOut()
                || vision.getCenterCamLargestNoteTarget().isPresent();
    }

    private Optional<NoteScanResult> getCenterCamNote() {
        if (vision.getCenterCamLargestNoteTarget().isPresent()) {
            return Optional.of(new NoteScanResult(
                    NoteDetectionSource.CenterCamera,
                    vision.getCenterCamLargestNoteTarget().get())
            );
        }
        return Optional.empty();
    }

    private Optional<NoteScanResult> getCornerCameraNote() {
        var notePosition = getClosestAvailableVisionNote();

        if (notePosition.isPresent()) {
            // transform the X/Y coordinate to a bearing
            double bearing = this.pose.getAngularErrorToTranslation2dInDegrees(
                    notePosition.get().getTranslation(),
                    Rotation2d.fromDegrees(180)); // point rear of robot
            SimpleNote fakeNote = new SimpleNote(100, bearing, 100);

            return Optional.of(new NoteScanResult(
                    NoteDetectionSource.PeripheralCameras,
                    fakeNote)
            );
        }
        return Optional.empty();
    }

    private Optional<NoteScanResult> scanForNote() {

        var centerNote = getCenterCamNote();
        if (centerNote.isPresent()) {
            return centerNote;
        }

        var cornerNote = getCornerCameraNote();
        if (cornerNote.isPresent()) {
            return cornerNote;
        }

        // Nothing found.
        return Optional.empty();
    }


    private Optional<Pose2d> getClosestAvailableVisionNote() {
        var virtualPoint = getProjectedPoint();
        var notePosition = this.oracle.getNoteMap().getClosestAvailableNote(virtualPoint, false);
        if(notePosition != null) {
            return Optional.of(notePosition.toPose2d());
        } else {
            return Optional.empty();
        }
    }

    private Pose2d getProjectedPoint() {
        return this.pose.getCurrentPose2d().plus(new Transform2d(-0.4, 0, new Rotation2d()));
    }

    private void decideWhetherToGiveUpOrRotate() {
        if (allowRotationSearch) {
            log.info("Attempting to find one via rotation.");
            noteAcquisitionMode = NoteAcquisitionMode.SearchViaRotation;
            rotationSearchModeTimeoutTracker.start();
        } else {
            log.info("Giving up.");
            noteAcquisitionMode = NoteAcquisitionMode.GiveUp;
        }
    }

    private boolean hasSolidLockOnNoteWithCenterCamera() {
        // If the note is roughly centered on the center camera, we can try driving to it.
        var target = vision.getCenterCamLargestNoteTarget();
        if (target.isPresent()) {
            return Math.abs(target.get().getYaw()) < 15;
        }
        return false;
    }


}
