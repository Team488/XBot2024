package competition.subsystems.drive.commands;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.NoteAcquisitionMode;
import competition.subsystems.vision.NoteSeekAdvice;
import competition.subsystems.vision.NoteSeekLogic;
import competition.subsystems.vision.VisionRange;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;

public class DriveToGivenNoteWithBearingVisionCommand extends DriveToGivenNoteCommand {

    final DynamicOracle oracle;
    final PoseSubsystem pose;
    final DriveSubsystem drive;
    final VisionSubsystem vision;
    final CollectorSubsystem collector;
    protected final NoteSeekLogic noteSeekLogic;

    NoteSeekAdvice lastAdvice;
    NoteSeekAdvice currentAdvice;

    @Inject
    DriveToGivenNoteWithBearingVisionCommand(PoseSubsystem pose, DriveSubsystem drive, DynamicOracle oracle,
                                             PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                             VisionSubsystem vision, CollectorSubsystem collector, NoteSeekLogic noteSeekLogic) {
        super(drive, oracle, pose, pf, headingModuleFactory);
        this.oracle = oracle;
        this.pose = pose;
        this.drive = drive;
        this.vision = vision;
        this.collector = collector;
        this.headingModule = headingModuleFactory.create(drive.getAggressiveGoalHeadingPid());

        logic.setPrioritizeRotationIfCloseToGoal(true);
        logic.setDistanceThresholdToPrioritizeRotation(1.5);

        this.noteSeekLogic = noteSeekLogic;
    }

    public void setVisionRangeOverride(VisionRange range) {
        noteSeekLogic.setVisionRange(range);
    }

    @Override
    public void initialize() {
        // The init here takes care of going to the initially given "static" note position.
        super.initialize();

        lastAdvice = new NoteSeekAdvice(NoteAcquisitionMode.BlindApproach,
                Optional.empty(), Optional.empty());
        noteSeekLogic.reset();
    }

    @Override
    public void execute() {

        currentAdvice = noteSeekLogic.getAdvice(super.isFinished());

        aKitLog.record("NoteAcquisitionMode", currentAdvice.noteAcquisitionMode);
        if (currentAdvice.suggestedPose.isPresent()) {
            aKitLog.record("SuggestedPose", currentAdvice.suggestedPose.get());
        }
        if (currentAdvice.suggestedDrivePercentages.isPresent()) {
            aKitLog.record("SuggestedDrivePercentages", currentAdvice.suggestedDrivePercentages.get());
        }

        boolean stateChanged = currentAdvice.noteAcquisitionMode != lastAdvice.noteAcquisitionMode;

        if (stateChanged) {
            if (currentAdvice.noteAcquisitionMode == NoteAcquisitionMode.BackAwayToTryAgain) {
                if (currentAdvice.suggestedPose.isPresent()) {
                    setBackingAwayFromNoteTarget(currentAdvice.suggestedPose.get());
                }
            }
        }

        switch (currentAdvice.noteAcquisitionMode) {
            // If we are going to specific locations, use the underlying SwerveSimpleTrajectoryCommand
            case BlindApproach:
            case BackAwayToTryAgain:
                super.execute();
                break;
            // If we are using
            case VisionApproach:
            case VisionTerminalApproach:
            case SearchViaRotation:
                if (currentAdvice.suggestedDrivePercentages.isPresent()) {
                    var driveValues = currentAdvice.suggestedDrivePercentages.get();
                    drive.move(new XYPair(driveValues.dx, driveValues.dy), driveValues.dtheta);
                }
                break;
            default:
                break;
        }

        lastAdvice = currentAdvice;
    }

    private void setBackingAwayFromNoteTarget(Pose2d suggestedPose) {
        var newTarget = suggestedPose.getTranslation();

        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(new XbotSwervePoint(newTarget, pose.getCurrentHeading(), 10));
        // when driving to a note, the robot must face backwards, as the robot's intake is on the back
        this.logic.setKeyPoints(swervePoints);
        this.logic.setAimAtGoalDuringFinalLeg(false);
        this.logic.setDriveBackwards(false);
        this.logic.setEnableConstantVelocity(true);
        reset();
    }

    @Override
    public boolean isFinished() {
        return collector.confidentlyHasControlOfNote()
                || currentAdvice.noteAcquisitionMode == NoteAcquisitionMode.GiveUp;
    }
}
