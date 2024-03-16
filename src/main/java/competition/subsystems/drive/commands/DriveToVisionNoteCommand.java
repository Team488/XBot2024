package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class DriveToVisionNoteCommand extends DriveToGivenNoteCommand {

    @Inject
    public DriveToVisionNoteCommand(DriveSubsystem drive, DynamicOracle oracle,
                                    PoseSubsystem pose, PropertyFactory pf,
                                    HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, oracle, pose, pf, headingModuleFactory);
    }

    @Override
    public void initialize() {
        var bestNote = oracle.getNoteMap().getClosestAvailableNote(pose.getCurrentPose2d(), false);
        drive.setTargetNote(bestNote.toPose2d());
        super.initialize();
    }
}
