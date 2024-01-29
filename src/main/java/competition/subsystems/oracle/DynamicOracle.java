package competition.subsystems.oracle;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import xbot.common.command.BaseSubsystem;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.trajectory.LowResField;
import xbot.common.trajectory.Obstacle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class DynamicOracle extends BaseSubsystem {

    public enum HighLevelGoal {
        CollectNote,
        ScoreInAmp,
        ScoreInSpeaker
    }

    public enum ScoringSubGoals {
        IngestNote,
        ControlNote,
        PrepareToScore,
        EarnestlyLaunchNote,
        NoteExitedRobot
    }

    NoteCollectionInfoSource noteCollectionInfoSource;
    NoteFiringInfoSource noteFiringInfoSource;
    NoteMap noteMap;
    LowResField field;

    HighLevelGoal currentHighLevelGoal;
    boolean firstRunInNewGoal;

    PoseSubsystem pose;

    int instructionNumber = 0;

    @Inject
    public DynamicOracle(NoteCollectionInfoSource noteCollectionInfoSource, NoteFiringInfoSource noteFiringInfoSource,
                         PoseSubsystem pose) {
        this.noteCollectionInfoSource = noteCollectionInfoSource;
        this.noteFiringInfoSource = noteFiringInfoSource;
        this.noteMap = new NoteMap();
        this.pose = pose;

        this.currentHighLevelGoal = HighLevelGoal.CollectNote;
        firstRunInNewGoal = true;
        setupLowResField();

        reserveNote(Note.KeyNoteNames.BlueSpikeMiddle);
        reserveNote(Note.KeyNoteNames.BlueSpikeBottom);

        Pose2d scoringPositionTop = new Pose2d(1.3, 6.9, Rotation2d.fromDegrees(0));
        Pose2d scoringPositionMiddle = new Pose2d(1.5, 5.5, Rotation2d.fromDegrees(0));
        Pose2d scoringPositionBottom = new Pose2d(0.9, 4.3, Rotation2d.fromDegrees(0));

        activeScoringPosition = scoringPositionTop;
        createRobotObstacle(scoringPositionMiddle.getTranslation(), 1.75, "PartnerA");
        createRobotObstacle(scoringPositionBottom.getTranslation(), 1.75, "PartnerB");
    }

    Pose2d activeScoringPosition;

    int noteCount = 1;
    private void reserveNote(Note.KeyNoteNames specificNote) {
        Note reserved = noteMap.getNote(specificNote);
        reserved.setAvailability(Note.NoteAvailability.ReservedByOthersInAuto);
        // create an obstacle at the same location.
        field.addObstacle(new Obstacle(reserved.getLocation().getTranslation().getX(),
                reserved.getLocation().getTranslation().getY(), 1.25, 1.25, "ReservedNote" + noteCount));
        noteCount++;
    }

    private void createRobotObstacle(Translation2d location, double sideLength, String name) {
        field.addObstacle(new Obstacle(location.getX(), location.getY(), sideLength, sideLength, name));
    }

    private Obstacle createObstacleWithRobotWidth(Translation2d location, double width, double height,
                                                  double robotWidth, String name, LowResField field) {
        return createObstacleWithRobotWidth(location.getX(), location.getY(), width, height, robotWidth, name, field);
    }



    private Obstacle createObstacleWithRobotWidth(double x, double y, double width, double height,
                                              double robotWidth, String name, LowResField field) {
        var obstacle = new Obstacle(x, y, width+robotWidth, height+robotWidth, name);
        field.addObstacle(obstacle);
        return obstacle;
    }

    private LowResField setupLowResField() {
        field = new LowResField();
        // For now, just add the three columns in the middle.
        // !!These values seem incorrect on simulator!!.
        // createObstacleWithRobotWidth(3.2004, 4.105656, 0.254,0.254, .914, "BlueLeftStageColumn", field);
        // widths and height are different to account for angle differences
        //createObstacleWithRobotWidth(5.8129, 5.553456, 0.3469,0.3469, .914, "BlueTopStageColumn", field);
        //createObstacleWithRobotWidth(5.8129,  2.657856,0.3469, 0.3469, .914, "BlueBottomStageColumn", field);

        // Blue obstacles
        createObstacleWithRobotWidth(PoseSubsystem.BlueLeftStageColumn,
                PoseSubsystem.closeColumnWidth, PoseSubsystem.closeColumnWidth, .914, "BlueLeftStageColumn", field);
        createObstacleWithRobotWidth(PoseSubsystem.BlueTopStageColumn,
                PoseSubsystem.farColumnWidths, PoseSubsystem.farColumnWidths, .914,"BlueTopStageColumn", field);
        createObstacleWithRobotWidth(PoseSubsystem.BlueBottomStageColumn,
                PoseSubsystem.farColumnWidths, PoseSubsystem.farColumnWidths, .914, "BlueBottomStageColumn", field);

        var speaker = createObstacleWithRobotWidth(PoseSubsystem.BlueSubwoofer,
                PoseSubsystem.SubwooferWidth, PoseSubsystem.SubwooferHeight, .914, "BlueSubwoofer", field);
        speaker.defaultBottomLeft = false;
        speaker.defaultTopLeft = false;

        // Red obstacles
        createObstacleWithRobotWidth(BasePoseSubsystem.convertBlueToRed(PoseSubsystem.BlueLeftStageColumn),
                PoseSubsystem.closeColumnWidth, PoseSubsystem.closeColumnWidth, .914, "RedLeftStageColumn", field);
        createObstacleWithRobotWidth(BasePoseSubsystem.convertBlueToRed(PoseSubsystem.BlueTopStageColumn),
                PoseSubsystem.farColumnWidths, PoseSubsystem.farColumnWidths, .914, "RedTopStageColumn", field);
        createObstacleWithRobotWidth(BasePoseSubsystem.convertBlueToRed(PoseSubsystem.BlueBottomStageColumn),
                PoseSubsystem.farColumnWidths, PoseSubsystem.farColumnWidths, .914, "RedBottomStageColumn", field);

        speaker = createObstacleWithRobotWidth(BasePoseSubsystem.convertBlueToRed(PoseSubsystem.BlueSubwoofer),
                PoseSubsystem.SubwooferWidth, PoseSubsystem.SubwooferHeight, .914, "RedSubwoofer", field);
        speaker.defaultBottomLeft = false;
        speaker.defaultTopLeft = false;

        return field;
    }

    public LowResField getFieldWithObstacles() {
        return field;
    }

    public void overrideGoal(HighLevelGoal goal) {
        if (goal != currentHighLevelGoal) {
            firstRunInNewGoal = true;
        }
        this.currentHighLevelGoal = goal;
    }

    public HighLevelGoal getHighLevelGoal() {
        return this.currentHighLevelGoal;
    }

    boolean reevaluationRequested = false;
    public void requestReevaluation() {
        reevaluationRequested = true;
    }

    @Override
    public void periodic() {

        switch (currentHighLevelGoal) {
            case ScoreInAmp: // For now keeping things simple
            case ScoreInSpeaker:
                if (firstRunInNewGoal || reevaluationRequested) {
                    setTargetNote(null);
                    setTerminatingPoint(activeScoringPosition);

                    setSpecialAimTarget(new Pose2d(0, 5.5, Rotation2d.fromDegrees(0)));
                    // Choose a good speaker scoring location
                    // Publish a route from current position to that location
                    firstRunInNewGoal = false;
                    reevaluationRequested = false;
                }
                // If we've launched our note, time to get another one
                if (noteFiringInfoSource.confidentlyHasFiredNote()) {
                    currentHighLevelGoal = HighLevelGoal.CollectNote;
                    firstRunInNewGoal = true;
                    break;
                }
                break;
            case CollectNote:
                if (firstRunInNewGoal || reevaluationRequested) {
                    // Choose a good note collection location
                    Note suggestedNote = noteMap.getClosestNote(pose.getCurrentPose2d().getTranslation(),
                            Note.NoteAvailability.Available,
                            Note.NoteAvailability.SuggestedByDriver,
                            Note.NoteAvailability.SuggestedByVision);
                    setTargetNote(suggestedNote);
                    setSpecialAimTarget(null);
                    if (suggestedNote == null) {
                        // No notes on the field! Let's suggest going to the source and hope something turns up.
                        setTerminatingPoint(new Pose2d(14, 1.2, Rotation2d.fromDegrees(0)));
                    } else {
                        setTerminatingPoint(getTargetNote().getLocation());
                    }
                    // Publish a route from current position to that location
                    firstRunInNewGoal = false;
                    reevaluationRequested = false;
                }

                if (noteCollectionInfoSource.confidentlyHasControlOfNote()) {
                    // Mark the nearest note as being unavailable, if we are anywhere near it
                    Note nearestNote = noteMap.getClosestNote(pose.getCurrentPose2d().getTranslation(), 1.0);
                    if (nearestNote != null) {
                        nearestNote.setAvailability(Note.NoteAvailability.Unavailable);
                    }

                    // Since we have a note, let's go score it.
                    currentHighLevelGoal = HighLevelGoal.ScoreInSpeaker;
                    firstRunInNewGoal = true;
                    break;
                }
                break;
            default:
                break;
        }

        // If we're getting close to our firing point, warm up the shooter
        if (getEstimatedSecondsUntilScoringRequired() < 2) {
            // Warm up the shooter
        }

        aKitLog.record("Current Goal", currentHighLevelGoal);
        aKitLog.record("Current Note",
                targetNote == null ? new Pose2d(-100, -100, new Rotation2d(0)) : getTargetNote().getLocation());
        aKitLog.record("Terminating Point", getTerminatingPoint().getTerminatingPose());
        aKitLog.record("MessageCount", getTerminatingPoint().getPoseMessageNumber());

        // Let's show some major obstacles
        field.getObstacles().forEach(obstacle -> {
            aKitLog.record(obstacle.getName(), obstacleToTrajectory(obstacle));
        });
    }

    private Trajectory obstacleToTrajectory(Obstacle o) {
        // create a trajectory using the 4 corners of the obstacle.
        ArrayList<Trajectory.State> wpiStates = new ArrayList<>();
        var topLeftcorner = new Trajectory.State();
        topLeftcorner.poseMeters = new Pose2d(o.topLeft, Rotation2d.fromDegrees(0));
        var topRightCorner = new Trajectory.State();
        topRightCorner.poseMeters = new Pose2d(o.topRight, Rotation2d.fromDegrees(0));
        var bottomLeftCorner = new Trajectory.State();
        bottomLeftCorner.poseMeters = new Pose2d(o.bottomLeft, Rotation2d.fromDegrees(0));
        var bottomRightCorner = new Trajectory.State();
        bottomRightCorner.poseMeters = new Pose2d(o.bottomRight, Rotation2d.fromDegrees(0));

        wpiStates.add(topLeftcorner);
        wpiStates.add(topRightCorner);
        wpiStates.add(bottomRightCorner);
        wpiStates.add(bottomLeftCorner);
        wpiStates.add(topLeftcorner);

        return new Trajectory(wpiStates);
    }

    Note targetNote;
    private void setTargetNote(Note note) {
        this.targetNote = note;
    }

    private Note getTargetNote() {
        return this.targetNote;
    }

    OracleTerminatingPoint terminatingPoint;
    Pose2d specialAimTarget;

    private void setTerminatingPoint(Pose2d point) {
        this.terminatingPoint = new OracleTerminatingPoint(point, instructionNumber++);
    }

    private void setSpecialAimTarget(Pose2d point) {
        this.specialAimTarget = point;
    }

    public OracleTerminatingPoint getTerminatingPoint() {
        return this.terminatingPoint;
    }

    public Pose2d getSpecialAimTarget() {
        return this.specialAimTarget;
    }

    private double getEstimatedSecondsUntilScoringRequired() {
        return 0;
    }
}
