package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.SimpleNote;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;

public class PointAtNoteWithBearingCommand extends BaseCommand {
    final Logger log = LogManager.getLogger(this);

    SimpleNote savedNotePosition = null;
    final DriveSubsystem drive;
    final HeadingModule headingModule;
    final PoseSubsystem pose;
    final OperatorInterface oi;
    final VisionSubsystem vision;
    final DoubleProperty minNoteArea;

    @Inject
    public PointAtNoteWithBearingCommand(DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PoseSubsystem pose,
                                         OperatorInterface oi, VisionSubsystem vision, PropertyFactory pf) {
        this.drive = drive;
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.pose = pose;
        this.oi = oi;
        this.vision = vision;

        pf.setPrefix(this);
        this.minNoteArea = pf.createPersistentProperty("Minimum note area", 10);
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        savedNotePosition = null;
        // Find the note we want to point at
        var largestTarget = getLargestTarget();
        if (largestTarget != null) {
            this.savedNotePosition = largestTarget;
            log.info("Rotating to note, current rotation error: {}",
                    largestTarget.getYaw());
        } else {
            this.savedNotePosition = null;
            log.warn("No note found to rotate to");
        }
    }

    @Override
    public void execute() {
        if (savedNotePosition == null) {
            log.warn("Skipping execute due to no target.");
            return;
        }

        // If we can still see the note, update the target
        var newTarget = getLargestTarget();
        if (newTarget != null && newTarget != this.savedNotePosition) {
            this.savedNotePosition = newTarget;
        }

        var toNoteTranslation = this.pose.getCurrentPose2d()
                .transformBy(new Transform2d(1.0, 0.0, new Rotation2d()))
                .getTranslation();
        // if we're very close to the note, stop trying to rotate, it gets wonky

        var movement = MathUtils.deadband(
                getDriveIntent(toNoteTranslation, oi.driverGamepad.getLeftVector(),
                        DriverStation.getAlliance().orElse(Alliance.Blue)),
                oi.getDriverGamepadTypicalDeadband(), (x) -> x);
        double rotationPower = 0;
        // if we're far enough away, rotate towards the note (if we're too close, the )
        if (toNoteTranslation.getNorm() > this.minNoteArea.get()) {
            rotationPower = this.drive.getRotateToHeadingPid().calculate(0, savedNotePosition.getYaw());
        }

        // negative movement because we want to drive the robot 'backwards', collector towards the note
        drive.move(new XYPair(-movement, 0), rotationPower);

        aKitLog.record("YawToTarget", this.savedNotePosition.getYaw());
    }

    public static double getDriveIntent(Translation2d fieldTranslationToTarget, XYPair driveJoystick, Alliance alliance) {
        var toNoteVector = fieldTranslationToTarget.toVector().unit();
        var driverVector = VecBuilder.fill(driveJoystick.y, -driveJoystick.x);
        if(alliance == Alliance.Red) {
            // invert both axis
            driverVector = driverVector.div(-1);
        }
        var dot = toNoteVector.dot(driverVector);

        return dot;
    }

    @Override
    public boolean isFinished() {
        if (this.savedNotePosition == null) {
            log.warn("Command finished due to no note.");
            return true;
        }
        return false;
    }

    private SimpleNote getLargestTarget() {
        var targets = vision.getCenterlineDetections();
        if (targets.length == 0) {
            return null;
        }
        return Arrays.stream(targets)
                .filter(t -> t.getArea() > this.minNoteArea.get())
                .max(Comparator.comparingDouble(SimpleNote::getArea))
                .orElse(null);
    }
}
