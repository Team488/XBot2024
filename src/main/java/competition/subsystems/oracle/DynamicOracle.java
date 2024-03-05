package competition.subsystems.oracle;

import competition.navigation.GraphField;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.sensors.buttons.AdvancedTrigger;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.trajectory.LowResField;
import xbot.common.trajectory.Obstacle;
import xbot.common.trajectory.ProvidesWaypoints;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;

@Singleton
public class DynamicOracle extends BaseSubsystem {

    public enum HighLevelGoal {
        NoGoal,
        CollectNote,
        ScoreInAmp,
        ScoreInSpeaker
    }

    public enum ScoringSubGoals {
        IngestNote,
        IngestNoteAgainstObstacle,
        MoveToScoringRange,
        EarnestlyLaunchNote
    }

    NoteCollectionInfoSource noteCollectionInfoSource;
    NoteFiringInfoSource noteFiringInfoSource;
    NoteMap noteMap;
    ScoringLocationMap scoringLocationMap;
    GraphField field;

    HighLevelGoal currentHighLevelGoal;
    ScoringSubGoals currentScoringSubGoal;
    boolean firstRunInNewGoal;

    PoseSubsystem pose;
    VisionSubsystem vision;
    ArmSubsystem arm;
    OperatorInterface oi;

    final BooleanProperty includeVisionNotes;
    final DoubleProperty maxVisionNoteAge;

    int instructionNumber = 0;
    double robotWidth = 0.914+0.5;
    double scoringZoneOffset = 0.93;

    private final AdvancedTrigger reserveTopSubwooferButton;
    private final AdvancedTrigger reserveMiddleSubwooferButton;
    private final AdvancedTrigger reserveBottomSubwooferButton;
    private final AdvancedTrigger reserveTopSpikeButton;
    private final AdvancedTrigger reserveMiddleSpikeButton;
    private final AdvancedTrigger reserveBottomSpikeButton;
    private final AdvancedTrigger reserveCenterLine1Button;
    private final AdvancedTrigger reserveCenterLine2Button;
    private final AdvancedTrigger reserveCenterLine3Button;
    private final AdvancedTrigger reserveCenterLine4Button;
    private final AdvancedTrigger reserveCenterLine5Button;


    @Inject
    public DynamicOracle(NoteCollectionInfoSource noteCollectionInfoSource, NoteFiringInfoSource noteFiringInfoSource,
                         PoseSubsystem pose, VisionSubsystem vision, ArmSubsystem arm, OperatorInterface oi,
                         PropertyFactory pf) {
        this.noteCollectionInfoSource = noteCollectionInfoSource;
        this.noteFiringInfoSource = noteFiringInfoSource;
        this.noteMap = new NoteMap();
        this.scoringLocationMap = new ScoringLocationMap();

        this.pose = pose;
        this.vision = vision;
        this.arm = arm;
        this.oi = oi;

        pf.setPrefix(this);
        this.includeVisionNotes = pf.createPersistentProperty("IncludeVisionNotes", true);
        this.maxVisionNoteAge = pf.createPersistentProperty("MaxVisionNoteAge", 1.0);

        this.currentHighLevelGoal = HighLevelGoal.CollectNote;
        this.currentScoringSubGoal = ScoringSubGoals.EarnestlyLaunchNote;
        firstRunInNewGoal = true;

        reserveTopSubwooferButton = oi.neoTrellis.getifAvailable(25);
        reserveMiddleSubwooferButton = oi.neoTrellis.getifAvailable(26);
        reserveBottomSubwooferButton = oi.neoTrellis.getifAvailable(27);
        reserveTopSpikeButton = oi.neoTrellis.getifAvailable(10);
        reserveMiddleSpikeButton = oi.neoTrellis.getifAvailable(11);
        reserveBottomSpikeButton = oi.neoTrellis.getifAvailable(12);
        reserveCenterLine1Button = oi.neoTrellis.getifAvailable(2);
        reserveCenterLine2Button = oi.neoTrellis.getifAvailable(3);
        reserveCenterLine3Button = oi.neoTrellis.getifAvailable(4);
        reserveCenterLine4Button = oi.neoTrellis.getifAvailable(5);
        reserveCenterLine5Button = oi.neoTrellis.getifAvailable(6);
    }

    Pose2d activeScoringPosition;

    private void reserveNote(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        Note reserved = noteMap.get(pointOfInterest, alliance);
        reserved.setAvailability(Availability.ReservedByOthersInAuto);
    }

    /**
     * Reserve a scoring pointOfInterest, marking it inaccessible to the robot during autonomous.
     * @param pointOfInterest The scoring pointOfInterest to mark as reserved by others during auto
     */
    public void reserveScoringLocationForOtherTeams(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        // Mark it as reserved in the map, so we don't try to navigate there
        scoringLocationMap.get(pointOfInterest, alliance).setAvailability(Availability.ReservedByOthersInAuto);
        // create the relevant obstacle so we path around any robot chilling there
    }

    private void setupField() {
        field = new GraphField();
    }

    public ProvidesWaypoints getFieldWithObstacles() {
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

    /**
     * Should be called in AutonomousInit.
     * Idea: the operator will use the NeoTrellis to lay out a plan, and then either press a button to
     * lock in that plan (or make updates to the plan), or the plan will automatically lock in at the start of auto.
     */
    public void freezeConfigurationForAutonomous() {
        setupField();
        noteMap = new NoteMap();
        scoringLocationMap = new ScoringLocationMap();

        DriverStation.Alliance ourAlliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue);
        DriverStation.Alliance allianceToMarkAsUnavailable;
        if (ourAlliance == DriverStation.Alliance.Blue) {
            allianceToMarkAsUnavailable = DriverStation.Alliance.Red;
        } else{
            allianceToMarkAsUnavailable = DriverStation.Alliance.Blue;
        }

        // Disable things that aren't possible
        noteMap.get(PointOfInterest.SpikeTop, allianceToMarkAsUnavailable).setAvailability(Availability.Unavailable);
        noteMap.get(PointOfInterest.SpikeMiddle, allianceToMarkAsUnavailable).setAvailability(Availability.Unavailable);
        noteMap.get(PointOfInterest.SpikeBottom, allianceToMarkAsUnavailable).setAvailability(Availability.Unavailable);
        scoringLocationMap.markAllianceScoringLocationsAsUnavailable(allianceToMarkAsUnavailable);

        // Disable subwoofer positions the driver has told us to avoid
        if (reserveTopSubwooferButton.getAsBoolean()) {
            reserveScoringLocationForOtherTeams(PointOfInterest.SubwooferTopScoringLocation, ourAlliance);
        }
        if (reserveMiddleSubwooferButton.getAsBoolean()) {
            reserveScoringLocationForOtherTeams(PointOfInterest.SubwooferMiddleScoringLocation, ourAlliance);
        }
        if (reserveBottomSubwooferButton.getAsBoolean()) {
            reserveScoringLocationForOtherTeams(PointOfInterest.SubwooferBottomScoringLocation, ourAlliance);
        }
        // Disable notes the driver told us to avoid
        if (reserveTopSpikeButton.getAsBoolean()) {
            reserveNote(PointOfInterest.SpikeTop, ourAlliance);
        }
        if (reserveMiddleSpikeButton.getAsBoolean()) {
            reserveNote(PointOfInterest.SpikeMiddle, ourAlliance);
        }
        if (reserveBottomSpikeButton.getAsBoolean()) {
            reserveNote(PointOfInterest.SpikeBottom, ourAlliance);
        }

        // If the bottom spike is available, then we need to suppress the scoring location there until it is collected.
        if (!reserveBottomSpikeButton.getAsBoolean()) {
            scoringLocationMap.get(PointOfInterest.PodiumScoringLocation, ourAlliance).setAvailability(Availability.MaskedByNote);
            field.getNode(PointOfInterest.PodiumScoringLocation.getName(ourAlliance)).setAllWeightsToMax();
        }

        // Disable center line notes the driver told us to avoid
        // This says we are reserving for blue, but the underlying layer will detect
        // these are unique. (I wish there was a "invalid" alliance).
        if (reserveCenterLine1Button.getAsBoolean()) {
            reserveNote(PointOfInterest.CenterLine1, DriverStation.Alliance.Blue);
        }
        if (reserveCenterLine2Button.getAsBoolean()) {
            reserveNote(PointOfInterest.CenterLine2, DriverStation.Alliance.Blue);
        }
        if (reserveCenterLine3Button.getAsBoolean()) {
            reserveNote(PointOfInterest.CenterLine3, DriverStation.Alliance.Blue);
        }
        if (reserveCenterLine4Button.getAsBoolean()) {
            reserveNote(PointOfInterest.CenterLine4, DriverStation.Alliance.Blue);
        }
        if (reserveCenterLine5Button.getAsBoolean()) {
            reserveNote(PointOfInterest.CenterLine5, DriverStation.Alliance.Blue);
        }
    }

    /**
     * The core state machine for the Oracle. Reads everything it can from the robot and its environment,
     * and then recommends actions. Other commands like SwerveAcordingToOracle or
     * SuperstructureControlAccordingToOracle may act on those recommendations.
     */
    @Override
    public void periodic() {
        checkForPodiumShotBecomingAvailable();

        // Populate the field with notes from vision
        noteMap.clearStaleVisionNotes(this.maxVisionNoteAge.get());
        if (this.includeVisionNotes.get()) {
            var robotTranslation = pose.getCurrentPose2d().getTranslation();
            var robotRotation = pose.getCurrentPose2d().getRotation();
            Arrays.stream(vision.getDetectedNotes())
                    .map(note -> {
                        var noteRelativeToRobot = new Translation2d(note.getX(), note.getY());
                        var rotatedToFieldRelative = noteRelativeToRobot.rotateBy(robotRotation);
                        return new Pose2d(
                                robotTranslation.plus(rotatedToFieldRelative),
                                new Rotation2d());
                    })
                    .forEach(noteMap::addVisionNote);
        }

        aKitLog.record("NoteMap", noteMap.getAllKnownNotes());

        switch (currentHighLevelGoal) {
            case ScoreInAmp: // For now keeping things simple
            case ScoreInSpeaker:
                if (firstRunInNewGoal || reevaluationRequested) {
                    setTargetNote(null);
                    var closestScoringLocation = scoringLocationMap.getClosest(pose.getCurrentPose2d().getTranslation(),
                            Availability.Available);
                    setTerminatingPoint(closestScoringLocation.getLocation());
                    setChosenScoringLocation(closestScoringLocation.getPointOfInterest());

                    currentScoringSubGoal = ScoringSubGoals.MoveToScoringRange;
                    setSpecialAimTarget(PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_AIM_TARGET));
                    // Choose a good speaker scoring location
                    // Publish a route from current position to that location
                    firstRunInNewGoal = false;
                    reevaluationRequested = false;
                }

                determineScoringSubgoal();

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
                    Note suggestedNote = noteMap.getClosest(pose.getCurrentPose2d().getTranslation(),
                            Availability.Available,
                            Availability.AgainstObstacle,
                            Availability.SuggestedByDriver,
                            Availability.SuggestedByVision);
                    setTargetNote(suggestedNote);

                    if (suggestedNote == null) {
                        // No notes on the field! Let's suggest going to the source and hope something turns up.
                        setTerminatingPoint(PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.NearbySource));
                    }/*
                    else if (suggestedNote.getAvailability() == Availability.AgainstObstacle) {
                        // Take the note's pose2d and extend it in to the super far distance so the robot
                        // will effectively aim at the "wall" of the obstacle as it approaches the note.
                        Pose2d superExtendedIntoTheDistancePose = new Pose2d(
                                new Translation2d(
                                        suggestedNote.getLocation().getTranslation().getX()
                                                + Math.cos(suggestedNote.getLocation().getRotation().getRadians()) * 10000000,
                                        suggestedNote.getLocation().getTranslation().getY()
                                                + Math.sin(suggestedNote.getLocation().getRotation().getRadians()) * 10000000),
                                suggestedNote.getLocation().getRotation()
                                );
                        setSpecialAimTarget(superExtendedIntoTheDistancePose);
                        setTerminatingPoint(suggestedNote.getLocation());
                    }*/
                    else {
                        setTerminatingPoint(getTargetNote().getLocation());
                        setSpecialAimTarget(getTargetNote().getLocation());
                    }

                    // Publish a route from current position to that location
                    firstRunInNewGoal = false;
                    reevaluationRequested = false;
                }

                currentScoringSubGoal = ScoringSubGoals.IngestNote;

                if (noteCollectionInfoSource.confidentlyHasControlOfNote()) {
                    // Mark the nearest note as being unavailable, if we are anywhere near it
                    Note nearestNote = noteMap.getClosest(pose.getCurrentPose2d().getTranslation(), 1.0);
                    if (nearestNote != null) {
                        nearestNote.setAvailability(Availability.Unavailable);
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

        /*
        if (field != null) {
            for (Pair<String,Trajectory> labelAndTrajectory : field.visualizeNodesAndEdges()) {
                aKitLog.record("Graph" + labelAndTrajectory.getFirst(), labelAndTrajectory.getSecond());
            }
        }*/

        aKitLog.record("Current Goal", currentHighLevelGoal);
        aKitLog.record("Current Note",
                targetNote == null ? new Pose2d(-100, -100, new Rotation2d(0)) : getTargetNote().getLocation());

        if (getTerminatingPoint() != null) {
            aKitLog.record("Terminating Point", getTerminatingPoint().getTerminatingPose());
            aKitLog.record("MessageCount", getTerminatingPoint().getPoseMessageNumber());
        }
        aKitLog.record("Current SubGoal", currentScoringSubGoal);
    }

    private void checkForPodiumShotBecomingAvailable() {
        // check to see if the podium note has been collected (it will be marked Unavailable).
        // If so, check our alliance, and restore the podium shot.
        checkForAllianceSpecificPodiumShotBecomingAvailable(DriverStation.Alliance.Blue);
        checkForAllianceSpecificPodiumShotBecomingAvailable(DriverStation.Alliance.Red);
    }

    private void checkForAllianceSpecificPodiumShotBecomingAvailable(DriverStation.Alliance alliance) {
        if (scoringLocationMap.get(PointOfInterest.PodiumScoringLocation, alliance).getAvailability() == Availability.MaskedByNote
                && DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == alliance
                && noteMap.get(PointOfInterest.SpikeBottom, alliance).getAvailability() == Availability.Unavailable) {
            scoringLocationMap.get(PointOfInterest.PodiumScoringLocation, alliance).setAvailability(Availability.Available);
            field.getNode(PointOfInterest.PodiumScoringLocation.getName(alliance)).restoreWeights();
        }
    }

    /**
     * Helper function that visualizes the bounding box of an obstacles using WPI trajectories,
     * which draw nicely on AdvantageScope
     * @param o The obstacle to visualize
     * @return A WPI trajectory that can be visualized by AdvantageScope
     */
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

    PointOfInterest chosenScoringLocation;
    public PointOfInterest getChosenScoringLocation() {
        return chosenScoringLocation;
    }

    public void setChosenScoringLocation(PointOfInterest location) {
        chosenScoringLocation = location;
    }

    private double getEstimatedSecondsUntilScoringRequired() {
        return 0;
    }

    public NoteMap getNoteMap() {
        return noteMap;
    }

    public ScoringSubGoals getScoringSubgoal() {
        return currentScoringSubGoal;
    }

    private void determineScoringSubgoal() {
        double acceptableRangeBeforeScoringMeters = 0;
        boolean inUnderstoodRange = false;
        if (currentHighLevelGoal == HighLevelGoal.ScoreInSpeaker) {
            acceptableRangeBeforeScoringMeters = 0.2;
            inUnderstoodRange = true; //pose.getDistanceFromSpeaker() < arm.getMaximumRangeForAnyShotMeters();
            // Since we currently only fire from "well known locations', this is true by default.
            // still leaving the above code commented out for when we (hopefully) get a smooth firing solution for
            // all ranges.

        } else if (currentHighLevelGoal == HighLevelGoal.ScoreInAmp) {
            // in the future we'll do something more like "get near amp, then drive into the wall for a few moments
            // before scoring"
            acceptableRangeBeforeScoringMeters = 0.05;
        }

        // TODO: also need to add a check to make sure our angular error is small enough
        double angularError = Math.abs(pose.getAngularErrorToTranslation2dInDegrees(specialAimTarget.getTranslation()));
        aKitLog.record("AngularErrorToSpecialTarget", angularError);
        boolean pointingAtSpeaker = angularError < 6.0;
        if (isTerminatingPointWithinDistance(acceptableRangeBeforeScoringMeters) && inUnderstoodRange && pointingAtSpeaker) {
            currentScoringSubGoal = ScoringSubGoals.EarnestlyLaunchNote;
        } else {
            currentScoringSubGoal = ScoringSubGoals.MoveToScoringRange;
        }
    }

    public boolean isTerminatingPointWithinDistance(double distance) {
        return pose.getCurrentPose2d().getTranslation().getDistance(
                getTerminatingPoint().getTerminatingPose().getTranslation())
                < distance;
    }

    public void resetNoteMap() {
        noteMap = new NoteMap();
    }
}
