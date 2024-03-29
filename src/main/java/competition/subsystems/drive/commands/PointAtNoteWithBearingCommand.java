package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.SimpleNote;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.VecBuilder;
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
import java.util.Optional;

public class PointAtNoteWithBearingCommand extends BaseCommand {
    final Logger log = LogManager.getLogger(this);

    Optional<SimpleNote> savedNotePosition = null;
    final DriveSubsystem drive;
    final HeadingModule headingModule;
    final PoseSubsystem pose;
    final OperatorInterface oi;
    final VisionSubsystem vision;
    final CollectorSubsystem collector;

    @Inject
    public PointAtNoteWithBearingCommand(DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PoseSubsystem pose,
                                         OperatorInterface oi, VisionSubsystem vision, PropertyFactory pf, CollectorSubsystem collector) {
        this.drive = drive;
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.pose = pose;
        this.oi = oi;
        this.vision = vision;
        this.collector = collector;

        pf.setPrefix(this);
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        savedNotePosition = Optional.empty();
        // Find the note we want to point at
        var largestTarget = vision.getCenterCamLargestNoteTarget();
        if (largestTarget.isPresent()) {
            this.savedNotePosition = largestTarget;
            log.info("Rotating to note, current rotation error: {}",
                    this.savedNotePosition.get().getYaw());
        } else {
            log.warn("No note found to rotate to");
        }
    }

    @Override
    public void execute() {
        // If we can still see the note, update the target
        this.savedNotePosition = vision.getCenterCamLargestNoteTarget();

        // Create a vector pointing 1m behind the robot, this might have a bug in it
        var toNoteTranslation = this.pose.getCurrentPose2d()
                .transformBy(new Transform2d(-1.0, 0.0, new Rotation2d()))
                .getTranslation();

        var movement = MathUtils.deadband(
                getDriveIntent(toNoteTranslation, oi.driverGamepad.getLeftVector(),
                        DriverStation.getAlliance().orElse(Alliance.Blue)),
                oi.getDriverGamepadTypicalDeadband(), (x) -> x);
        
        double rotationPower = 0;
        // if we see a note, rotate towards it
        if (savedNotePosition.isPresent()) {
            rotationPower = this.drive.getRotateToHeadingPid().calculate(0, savedNotePosition.get().getYaw());
        }

        // negative movement because we want to drive the robot 'backwards', collector towards the note
        drive.move(new XYPair(-movement, 0), rotationPower);

        if(savedNotePosition.isPresent()) {
            aKitLog.record("YawToTarget", this.savedNotePosition.get().getYaw());
        } else {
            aKitLog.record("YawToTarget", 0.0);
        }
        
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
        if (this.collector.getGamePieceInControl()) {
            log.warn("Note acquired, ending");
            return true;
        }
        return false;
    }
}
