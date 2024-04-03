package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
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
import xbot.common.logic.Latch;
import xbot.common.logic.HumanVsMachineDecider.HumanVsMachineDeciderFactory;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.drive.control_logic.HeadingModule.HeadingModuleFactory;

import javax.inject.Inject;
import java.util.Optional;

public class PointAtNoteWithBearingCommand extends SwerveDriveWithJoysticksCommand {
    final Logger log = LogManager.getLogger(this);

    Optional<SimpleNote> savedNotePosition = null;
    final DriveSubsystem drive;
    final HeadingModule headingModule;
    final PoseSubsystem pose;
    final OperatorInterface oi;
    final VisionSubsystem vision;
    final CollectorSubsystem collector;
    final DynamicOracle oracle;

    boolean everHadCenterNote = false;
    Latch everHadNoteLatch;

    @Inject
    public PointAtNoteWithBearingCommand(DriveSubsystem drive, HeadingModule.HeadingModuleFactory headingModuleFactory, PoseSubsystem pose,
                                         OperatorInterface oi, VisionSubsystem vision, PropertyFactory pf, CollectorSubsystem collector,
                                         DynamicOracle oracle, HumanVsMachineDeciderFactory hvmFactory) {
        super(drive, pose, oi, pf, hvmFactory, headingModuleFactory);
        this.drive = drive;
        this.headingModule = headingModuleFactory.create(drive.getAggressiveGoalHeadingPid());
        this.pose = pose;
        this.oi = oi;
        this.vision = vision;
        this.collector = collector;
        this.oracle = oracle;

        pf.setPrefix(this);
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        super.initialize();
        everHadCenterNote = false;
        everHadNoteLatch = new Latch(false, Latch.EdgeType.RisingEdge, (edge) -> {
            oi.driverGamepad.getRumbleManager().rumbleGamepad(0.8, 0.25);
            log.info("Acquired note, locking drive and rotating towards note");
        });
        savedNotePosition = Optional.empty();
        // Find the note we want to point at
        var largestTarget = vision.getCenterCamLargestNoteTarget();
        if (largestTarget.isPresent()) {
            everHadCenterNote = true;
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
        this.savedNotePosition.ifPresent((note) -> this.everHadCenterNote = true);
        this.everHadNoteLatch.setValue(this.everHadCenterNote);

        // Create a vector points towards the note in field-oriented heading
        var toNoteTranslation = new Translation2d(
                1.0,
                pose.getCurrentPose2d().getRotation()
                        // flip 180 because the camera is mounted backwards
                        .plus(Rotation2d.fromRotations(0.5))
                        .plus(
                            Rotation2d.fromDegrees(
                                savedNotePosition.map(note -> note.getYaw()).orElse(0.0)
                            )
                        )
            );

        var movement = MathUtils.deadband(
                getDriveIntent(toNoteTranslation, oi.driverGamepad.getLeftVector(),
                        DriverStation.getAlliance().orElse(Alliance.Blue)),
                oi.getDriverGamepadTypicalDeadband(), (x) -> x);
        
        double rotationPower = 0;
        // if we see a note clearly, rotate towards it
        if (savedNotePosition.isPresent() && savedNotePosition.get().getPitch() >= vision.terminalNotePitch){
            rotationPower = this.drive.getAggressiveGoalHeadingPid().calculate(0, savedNotePosition.get().getYaw());
            // negative movement because we want to drive the robot 'backwards', collector towards the note
            drive.move(new XYPair(-movement, 0), rotationPower);
        } else if (everHadCenterNote) {
            // negative movement because we want to drive the robot 'backwards', collector towards the note
            drive.move(new XYPair(-movement, 0), rotationPower);
        } else if (getClosestAvailableNote().isPresent()) {
            // try rotating towards the closest note
            everHadNoteLatch.setValue(true);
            var notePosition = getClosestAvailableNote().get();
            var rotationError = getRotationError(notePosition);
            rotationPower = this.drive.getAggressiveGoalHeadingPid().calculate(0, rotationError);
            // negative movement because we want to drive the robot 'backwards', collector towards the note
            drive.move(new XYPair(-movement, 0), rotationPower);
        } else {
            // fallback to normal driving
            super.execute();
        }

        
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

    private Optional<Pose2d> getClosestAvailableNote() {
        var virtualPoint = getProjectedPoint();
        var notePosition = this.oracle.getNoteMap().getClosestAvailableNote(virtualPoint, false);
        if(notePosition != null) {
            return Optional.of(notePosition.toPose2d());
        } else {
            return Optional.empty();
        }
    }

    private Pose2d getProjectedPoint() {
        return this.pose.getCurrentPose2d().plus(new Transform2d(-0.4, 0, new Rotation2d()));
    }

    private double getRotationError(Pose2d notePosition) {
        return this.pose.getAngularErrorToTranslation2dInDegrees(
                notePosition.getTranslation(),
                Rotation2d.fromDegrees(180)); // point rear of robot
    }
}
