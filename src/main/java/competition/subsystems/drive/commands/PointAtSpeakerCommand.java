package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorCommandMap;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.command.BaseCommand;
import xbot.common.logic.Latch;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class PointAtSpeakerCommand extends BaseCommand {
    double desiredHeading;
    double suggestedRotatePower;
    DriveSubsystem drive;
    HeadingModule headingModule;
    PoseSubsystem pose;
    OperatorInterface oi;
//    DoubleProperty turnPowerFactor;
//    Latch absoluteOrientationLatch;
//    BooleanProperty absoluteOrientationMode;

    @Inject
    public PointAtSpeakerCommand(DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PoseSubsystem pose,
                                 OperatorInterface oi, PropertyFactory pf) {
        this.drive = drive;
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.pose = pose;
        this.oi = oi;
//        this.turnPowerFactor = pf.createPersistentProperty("Turn Power Factor", 0.75);

//        this.absoluteOrientationMode = pf.createPersistentProperty("Absolute Orientation Mode", true);

//        // Set up a latch to trigger whenever we change the rotational mode. In either case,
//        // there's some PIDs that will need to be reset, or goals that need updating.
//        absoluteOrientationLatch = new Latch(absoluteOrientationMode.get(), Latch.EdgeType.Both, edge -> {
//            if(edge == Latch.EdgeType.RisingEdge) {
//                resetBeforeStartingAbsoluteOrientation();
//            }
//            else if(edge == Latch.EdgeType.FallingEdge) {
//                resetBeforeStartingRelativeOrientation();
//            }
//        });


        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void execute() {
        // Feed the latch with our mode state, so it can reset PIDs or goals as appropriate.
        // This will automatically reset the relevant PIDs - you can see what is exactly is registered
        // by looking at the latch code in the constructor.
//        absoluteOrientationLatch.setValue(absoluteOrientationMode.get());
        double rotateIntent = getSuggestedRotateIntent();
//        if (!drive.isUnlockFullDrivePowerActive()) {
//            rotateIntent *= turnPowerFactor.get();
//        }
        if (drive.isRobotOrientedDriveActive()) {
            drive.move(new XYPair(0,0), rotateIntent);
        } else {
            drive.fieldOrientedDrive(new XYPair(0,0), rotateIntent, pose.getCurrentHeading().getDegrees(), new XYPair(0,0));
        }
    }

    private double getRotationIntentPointAtSpeaker(Pose2d currentPose) {
        Translation2d speakerPosition = PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_TARGET_FORWARD);
        Translation2d currentXY = new Translation2d(currentPose.getX(), currentPose.getY());

        return currentXY.minus(speakerPosition).getAngle().getDegrees() + 180;
    }

    private double getSuggestedRotateIntent() {
        double suggestedRotatePower;
        // If we are using absolute orientation, we first need get the desired heading from the right joystick.
        // We need to only do this if the joystick has been moved past the minimumMagnitudeForAbsoluteHeading.
        // In the future, we might be able to replace the joystick with a dial or other device that can more easily
        // hold a heading.

        double desiredHeading = 0;
        desiredHeading = getRotationIntentPointAtSpeaker(pose.getCurrentPose2d());

        drive.setDesiredHeading(desiredHeading);

        suggestedRotatePower = headingModule.calculateHeadingPower(desiredHeading);

        return suggestedRotatePower;
    }
}
