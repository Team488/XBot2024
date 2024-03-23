package competition.commandgroups;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class DriveToGivenNoteWithVisionCommand extends DriveToGivenNoteCommand {

    DynamicOracle oracle;
    PoseSubsystem pose;
    DriveSubsystem drive;
    VisionSubsystem vision;
    CollectorSubsystem collector;
    boolean hasDoneVisionCheckYet = false;

    public enum NoteAcquisitionMode {
        BlindApproach,
        VisionApproach,
        BackAwayToTryAgain,
        GiveUp
    }

    private NoteAcquisitionMode noteAcquisitionMode = NoteAcquisitionMode.BlindApproach;

    @Inject
    DriveToGivenNoteWithVisionCommand(PoseSubsystem pose, DriveSubsystem drive, DynamicOracle oracle,
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
    }



    @Override
    public void execute() {

        // Check for mode changes
        switch (noteAcquisitionMode) {
            case BlindApproach:
                if (!hasDoneVisionCheckYet) {
                    if (pose.getCurrentPose2d().getTranslation().getDistance(
                            drive.getTargetNote().getTranslation()) < vision.getBestRangeFromStaticNoteToSearchForNote()) {
                        hasDoneVisionCheckYet = true;
                        log.info("Close to static note - attempting vision update.");
                        assignClosestVisionNoteToDriveSubsystemIfYouSeeANoteAndReplanPath();
                    }
                }
                break;
            case VisionApproach:
                if (super.isFinished()) {
                    // we've hit our target, but no note! Back away and try again.
                    log.info("Switching to back away mode");
                    setBackingAwayFromNoteTarget();
                    noteAcquisitionMode = NoteAcquisitionMode.BackAwayToTryAgain;
                }
                break;
            case BackAwayToTryAgain:
                if (super.isFinished()) {
                    // check to see if we see a note. If not, give up.
                    if (getNearestVisionNote() != null) {
                        log.info("Switching to vision mode");
                        assignClosestVisionNoteToDriveSubsystemIfYouSeeANoteAndReplanPath();
                        noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
                    } else {
                        log.info("Switching to give up mode");
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

        /*
        if (!hasDoneVisionCheckYet) {
            // check if we're close to the note.
            if (pose.getCurrentPose2d().getTranslation().getDistance(
                    drive.getTargetNote().getTranslation()) < vision.getBestRangeFromStaticNoteToSearchForNote()) {
                log.info("Checking to see if we have a vision note.");
                assignClosestVisionNoteToDriveSubsystemIfYouSeeANoteAndReplanPath();
                hasDoneVisionCheckYet = true;
            }
        }
        */
        super.execute();
    }

    private Pose2d getNearestVisionNote() {
        var notePose = oracle.getNoteMap().getClosestAvailableNote(
                pose.getCurrentPose2d(), false);
        if (notePose != null) {
            return notePose.toPose2d();
        }
        return null;
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

    private void assignClosestVisionNoteToDriveSubsystemIfYouSeeANoteAndReplanPath() {
        // Try to find a vision note
        var noteLocation = getNearestVisionNote();

        // If no notes, don't do anything
        if (noteLocation == null) {
            log.info("No notes found");
            return;
        }

        // If note is too far away, don't do anything
        if (noteLocation.getTranslation().getDistance(
                pose.getCurrentPose2d().getTranslation()) > vision.getMaxNoteSearchingDistanceForSpikeNotes()) {
            log.info("Note too far away");
            return;
        }

        log.info("Found note at " + noteLocation.getTranslation());
        log.info("Assigning note to drive subsystem");
        drive.setTargetNote(noteLocation);
        noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
        prepareToDriveAtGivenNote();
    }

    @Override
    public boolean isFinished() {
        return collector.confidentlyHasControlOfNote() || noteAcquisitionMode == NoteAcquisitionMode.GiveUp;
    }
}