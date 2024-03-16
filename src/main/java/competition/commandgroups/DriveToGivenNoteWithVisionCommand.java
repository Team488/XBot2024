package competition.commandgroups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class DriveToGivenNoteWithVisionCommand extends DriveToGivenNoteCommand {

    DynamicOracle oracle;
    PoseSubsystem pose;
    DriveSubsystem drive;

    double visionCheckDistance = 1.5;
    boolean hasDoneVisionCheckYet = false;

    @Inject
    DriveToGivenNoteWithVisionCommand(PoseSubsystem pose, DriveSubsystem drive, DynamicOracle oracle,
                                      PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, oracle, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.pose = pose;
        this.drive = drive;
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
                    drive.getTargetNote().getTranslation()) < visionCheckDistance) {
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
            return;
        }

        // If note is too far away, don't do aything
        if (noteLocation.toPose2d().getTranslation().getDistance(
                pose.getCurrentPose2d().getTranslation()) > visionCheckDistance*2) {
            return;
        }

        drive.setTargetNote(noteLocation.toPose2d());
        reset();
    }
}