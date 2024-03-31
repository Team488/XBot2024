package competition.subsystems.drive.commands;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class TryDriveToBearingNote extends DriveToGivenNoteWithBearingVisionCommand {

    @Inject
    public TryDriveToBearingNote(
            DriveSubsystem drive, DynamicOracle oracle, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            VisionSubsystem vision, CollectorSubsystem collector) {
        super(pose, drive, oracle, pf, headingModuleFactory, vision, collector);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        this.noteAcquisitionMode = NoteAcquisitionMode.VisionApproach;
        timeWhenTerminalVisionModeEntered = XTimer.getFPGATimestamp();
    }
}
