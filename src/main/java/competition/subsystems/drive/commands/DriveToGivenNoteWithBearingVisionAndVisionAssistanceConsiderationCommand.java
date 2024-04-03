package competition.subsystems.drive.commands;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.NoteSeekLogic;
import competition.subsystems.vision.VisionSubsystem;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class DriveToGivenNoteWithBearingVisionAndVisionAssistanceConsiderationCommand extends DriveToGivenNoteWithBearingVisionCommand{

    VisionSubsystem vision;

    @Inject
    DriveToGivenNoteWithBearingVisionAndVisionAssistanceConsiderationCommand(PoseSubsystem pose, DriveSubsystem drive, DynamicOracle oracle, PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory, VisionSubsystem vision, CollectorSubsystem collector, NoteSeekLogic noteSeekLogic) {
        super(pose, drive, oracle, pf, headingModuleFactory, vision, collector, noteSeekLogic);
        this.vision = vision;
    }

    @Override
    public void execute() {
        super.setMaximumSpeedOverride(vision.getSpeedForAuto());
        super.execute();
    }
}
