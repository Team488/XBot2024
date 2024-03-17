package competition.commandgroups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class DriveToGivenNoteWithVisionCommand extends DriveToGivenNoteCommand {

    DynamicOracle oracle;
    PoseSubsystem pose;
    DriveSubsystem drive;
    VisionSubsystem vision;
    boolean hasDoneVisionCheckYet = false;

    @Inject
    DriveToGivenNoteWithVisionCommand(PoseSubsystem pose, DriveSubsystem drive, DynamicOracle oracle,
                                      PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                      VisionSubsystem vision) {
        super(drive, oracle, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.pose = pose;
        this.drive = drive;
        this.vision = vision;
    }

    @Override
    public void initialize() {
        super.initialize();
        hasDoneVisionCheckYet = false;
    }


    @Override
    public void execute() {
        if (!hasDoneVisionCheckYet) {
            // check if we're close to the note.
            if (pose.getCurrentPose2d().getTranslation().getDistance(
                    drive.getTargetNote().getTranslation()) < vision.getBestRangeFromStaticNoteToSearchForNote()) {
                log.info("Checking to see if we have a vision note.");
                assignClosestVisionNoteToDriveSubsystemIfYouSeeANoteAndReplanPath();
                hasDoneVisionCheckYet = true;
            }
        }
        super.execute();
    }

    private void assignClosestVisionNoteToDriveSubsystemIfYouSeeANoteAndReplanPath() {
        // Try to find a vision note
        var noteLocation = oracle.getNoteMap().getClosestAvailableNote(
                pose.getCurrentPose2d(), false);

        // If no notes, don't do anything
        if (noteLocation == null) {
            log.info("No notes found");
            return;
        }

        // If note is too far away, don't do anything
        if (noteLocation.toPose2d().getTranslation().getDistance(
                pose.getCurrentPose2d().getTranslation()) > vision.getMaxNoteSearchingDistanceForSpikeNotes()) {
            log.info("Note too far away");
            return;
        }

        log.info("Found note at " + noteLocation.toPose2d().getTranslation());
        log.info("Assigning note to drive subsystem");
        drive.setTargetNote(noteLocation.toPose2d());

        prepareToDriveAtGivenNote();
    }
}