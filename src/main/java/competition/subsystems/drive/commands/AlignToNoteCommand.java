package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class AlignToNoteCommand extends BaseCommand {

    DriveSubsystem drive;
    VisionSubsystem vision;
    PoseSubsystem pose;
    private HeadingModule headingModule;

    @Inject
    public AlignToNoteCommand(DriveSubsystem drive, VisionSubsystem vision, PoseSubsystem pose,
                              HeadingModule.HeadingModuleFactory headingModuleFactory) {
        this.drive = drive;
        this.vision = vision;
        this.pose = pose;
        this.addRequirements(drive);
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());

    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        double yaw = 0;//vision.getNoteYaw();
        double area = 14;//vision.getNoteArea();
        double desiredNoteSize = 14;

        var headingGoal = pose.getCurrentHeading().minus(Rotation2d.fromDegrees(yaw));

        // Center the note
        double rotationPower = headingModule.calculateHeadingPower(headingGoal);

        // Get note nearby
        double forwardPower = 0;

        if (area < 0) {
            forwardPower = 0;
        } else {
            double sizeDelta = desiredNoteSize - area;
            // positive sizeDelta means we need to drive forward
            forwardPower = - ((sizeDelta / 3) * 1);
        }

        drive.move(new XYPair(forwardPower, 0), rotationPower);
    }
}
