package competition.subsystems.oracle;

import competition.navigation.GraphField;
import competition.navigation.Pose2dNode;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.advantage.AKitLogger;
import xbot.common.command.BaseSubsystem;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.trajectory.Obstacle;
import xbot.common.trajectory.ProvidesWaypoints;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class DynamicOracle extends BaseSubsystem {

    public enum HighLevelGoal {
        NoGoal,
        CollectNote,
        ScoreInAmp,
        ScoreInSpeaker
    }

    public enum ScoringSubGoals {
        IngestNoteBlindly,
        IngestNoteVisionTerminalApproach,
        IngestNoteBlindTerminalApproach,
        IngestFromSource,
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

    final PoseSubsystem pose;
    final VisionSubsystem vision;
    final ArmSubsystem arm;
    final OperatorInterface oi;

    final BooleanProperty includeVisionNotes;
    final DoubleProperty maxVisionNoteAge;

    int instructionNumber = 0;
    double robotWidth = 0.914+0.5;
    double scoringZoneOffset = 0.93;

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

        this.currentHighLevelGoal = HighLevelGoal.ScoreInSpeaker;
        this.currentScoringSubGoal = ScoringSubGoals.EarnestlyLaunchNote;
        firstRunInNewGoal = true;
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

        // Disable things that aren't possible in autonomous
        noteMap.get(PointOfInterest.SpikeTop, allianceToMarkAsUnavailable).setAvailability(Availability.Unavailable);
        noteMap.get(PointOfInterest.SpikeMiddle, allianceToMarkAsUnavailable).setAvailability(Availability.Unavailable);
        noteMap.get(PointOfInterest.SpikeBottom, allianceToMarkAsUnavailable).setAvailability(Availability.Unavailable);

        // Disable scoring locations for the other alliance - no reason to score in their stuff.
        scoringLocationMap.markAllianceScoringLocationsWithAvailability(allianceToMarkAsUnavailable, Availability.Unavailable);

        // Disable Scoring positions the driver has specifically told us to avoid
        reserveScoringLocationBasedOnNeoTrellis(PointOfInterest.SubwooferTopScoringLocation, ourAlliance);
        reserveScoringLocationBasedOnNeoTrellis(PointOfInterest.SubwooferMiddleScoringLocation, ourAlliance);
        reserveScoringLocationBasedOnNeoTrellis(PointOfInterest.SubwooferBottomScoringLocation, ourAlliance);
        reserveScoringLocationBasedOnNeoTrellis(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation, ourAlliance);
        reserveScoringLocationBasedOnNeoTrellis(PointOfInterest.MiddleSpikeScoringLocation, ourAlliance);
        reserveScoringLocationBasedOnNeoTrellis(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation, ourAlliance);

        // Disable Spike notes the driver told us to avoid
        reserveNoteBasedOnNeoTrellis(PointOfInterest.SpikeTop, ourAlliance);
        reserveNoteBasedOnNeoTrellis(PointOfInterest.SpikeMiddle, ourAlliance);
        reserveNoteBasedOnNeoTrellis(PointOfInterest.SpikeBottom, ourAlliance);

        // If the bottom spike is available, and we are allowed to shoot from the podium or near the bottom spike
        // then we need to suppress those scoring locations there until the note is collected.
        // (otherwise, we might collect the midline note first, then drive over the podium note since
        // the podium scoring location is very close).
        if (!oi.getNeoTrellisValue(PointOfInterest.SpikeBottom) && !oi.getNeoTrellisValue(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation)) {
            scoringLocationMap.get(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation, ourAlliance).setAvailability(Availability.MaskedByNote);
            field.getNode(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation.getName(ourAlliance)).setAllWeightsToMax();
        }

        // We also need to do this for the top spike
        if (!oi.getNeoTrellisValue(PointOfInterest.SpikeTop) && !oi.getNeoTrellisValue(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation)) {
            scoringLocationMap.get(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation, ourAlliance).setAvailability(Availability.MaskedByNote);
            field.getNode(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation.getName(ourAlliance)).setAllWeightsToMax();
        }

        // For completeness, we'd need to do this for the center note as well, even though the note and scoring position share the same node.
        if (!oi.getNeoTrellisValue(PointOfInterest.SpikeMiddle) && !oi.getNeoTrellisValue(PointOfInterest.MiddleSpikeScoringLocation)) {
            scoringLocationMap.get(PointOfInterest.MiddleSpikeScoringLocation, ourAlliance).setAvailability(Availability.MaskedByNote);
            // In this case, don't set any weights to max, as we need to be able to go to that location to collect the note.
        }

        // In general, disable the "one robot length away" shot as it should only be used in teleop.
        // Also, the podium shot, and the far amp shot
        scoringLocationMap.get(PointOfInterest.OneRobotAwayFromCenterSubwooferScoringLocation, ourAlliance).setAvailability(Availability.Unavailable);
        scoringLocationMap.get(PointOfInterest.PodiumScoringLocation, ourAlliance).setAvailability(Availability.Unavailable);
        scoringLocationMap.get(PointOfInterest.AmpFarScoringLocation, ourAlliance).setAvailability(Availability.Unavailable);

        // Disable center line notes the driver told us to avoid
        // This says we are reserving for blue, but the underlying layer will detect
        // these are unique. (I wish there was a "invalid" alliance).
        reserveNoteBasedOnNeoTrellis(PointOfInterest.CenterLine1, DriverStation.Alliance.Blue);
        reserveNoteBasedOnNeoTrellis(PointOfInterest.CenterLine2, DriverStation.Alliance.Blue);
        reserveNoteBasedOnNeoTrellis(PointOfInterest.CenterLine3, DriverStation.Alliance.Blue);
        reserveNoteBasedOnNeoTrellis(PointOfInterest.CenterLine4, DriverStation.Alliance.Blue);
        reserveNoteBasedOnNeoTrellis(PointOfInterest.CenterLine5, DriverStation.Alliance.Blue);

        chooseStartingLocationBasedOnReservations();

        /*
        if (field != null) {
            for (Pair<String,Trajectory> labelAndTrajectory : field.visualizeNodesAndEdges()) {
                aKitLog.record("Graph" + labelAndTrajectory.getFirst(), labelAndTrajectory.getSecond());
            }
        }
        */
    }

    /**
     * Should be called in AutonomousExit.
     * By the end of autonomous, the default notes will almost certainly
     * be unavailable, so we don't want to accidentally have the robot
     * orient at these predefined notes that have been picked up during auto.
     */
    public void clearNoteMapForTeleop() {
        noteMap.markSpikeNotesUnavailable();
    }

    public void clearScoringLocationsForTeleop() {
        scoringLocationMap.markAllianceScoringLocationsWithAvailability(
                DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue), Availability.Available);
    }

    private void chooseStartingLocationBasedOnReservations() {
        // Set the pose subsystem to whatever location is unreserved. Will prefer the center
        // if multiple are active.
        Pose2d chosenLocation = null;
        if (!oi.getNeoTrellisValue(PointOfInterest.SubwooferTopScoringLocation)) {
            chosenLocation = PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation);
        }
        if (!oi.getNeoTrellisValue(PointOfInterest.SubwooferBottomScoringLocation)) {
            chosenLocation = PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation);
        }
        if (!oi.getNeoTrellisValue(PointOfInterest.SubwooferMiddleScoringLocation)) {
            chosenLocation = PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation);
        }

        // Default to middle
        if (chosenLocation == null) {
            chosenLocation = PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation);
        }

        pose.setCurrentPosition(chosenLocation);
    }

    public void reserveScoringLocationBasedOnNeoTrellis(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        if (oi.getNeoTrellisValue(pointOfInterest)) {
            reserveScoringLocationForOtherTeams(pointOfInterest, alliance);
            field.getNode(pointOfInterest.getName(alliance)).setAllWeightsToMax();
        }
    }

    public void reserveNoteBasedOnNeoTrellis(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        if (oi.getNeoTrellisValue(pointOfInterest)) {
            reserveNote(pointOfInterest, alliance);
            field.getNode(pointOfInterest.getName(alliance)).setAllWeightsToMax();
        }
    }

    /**
     * The core state machine for the Oracle. Reads everything it can from the robot and its environment,
     * and then recommends actions. Other commands like SwerveAcordingToOracle or
     * SuperstructureControlAccordingToOracle may act on those recommendations.
     */
    @Override
    public void periodic() {
        checkForMaskedShotsBecomingAvailable();

        // Populate the field with notes from vision
        noteMap.clearStaleVisionNotes(this.maxVisionNoteAge.get());
        if (this.includeVisionNotes.get()) {
            var robotTranslation = pose.getCurrentPose2d().getTranslation();
            var robotRotation = pose.getCurrentPose2d().getRotation();
            if (vision.getDetectedNotes() != null) {
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
        }

        aKitLog.setLogLevel(AKitLogger.LogLevel.DEBUG);
        // TODO: move this visualization into Simulator2024. This is a lot of data for network tables.
        // We can always set the global log level to debug and replay the inputs to regenerate this data.
        aKitLog.record("NoteMap", noteMap.getAllKnownNotes());
        aKitLog.setLogLevel(AKitLogger.LogLevel.INFO);

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
                        // However, if we are in autonomous, we should instead just go to the line.
                        if (DriverStation.isAutonomous()) {
                            setTerminatingPoint(PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.CenterLine5));
                        } else {
                            setTerminatingPoint(PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSourceMiddle));
                        }
                    }
                    else {
                        setTerminatingPoint(getTargetNote().getLocation());
                        setSpecialAimTarget(getTargetNote().getLocation());
                    }

                    // Publish a route from current position to that location
                    firstRunInNewGoal = false;
                    reevaluationRequested = false;

                    currentScoringSubGoal = ScoringSubGoals.IngestNoteBlindly;
                }

                determineCollectionSubgoal();

                if (noteCollectionInfoSource.confidentlyHasControlOfNote()) {
                    // Mark the nearest note as being unavailable, if we are anywhere near it
                    Note nearestNote = noteMap.getClosest(pose.getCurrentPose2d().getTranslation(), 1.5);
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

        aKitLog.record("Current Goal", currentHighLevelGoal);
        aKitLog.record("Current Note",
                targetNote == null ? new Pose2d(-100, -100, new Rotation2d(0)) : getTargetNote().getLocation());

        aKitLog.setLogLevel(AKitLogger.LogLevel.DEBUG);
        if (getTerminatingPoint() != null) {
            aKitLog.record("Terminating Point", getTerminatingPoint().getTerminatingPose());
            aKitLog.record("MessageCount", getTerminatingPoint().getPoseMessageNumber());
        }
        aKitLog.setLogLevel(AKitLogger.LogLevel.INFO);
        aKitLog.record("Current SubGoal", currentScoringSubGoal);
    }

    private void determineCollectionSubgoal() {
        if (currentScoringSubGoal == ScoringSubGoals.IngestNoteBlindly) {
            if (isTerminatingPointWithinDistance(vision.getBestRangeFromStaticNoteToSearchForNote())) {
                // look for a vision note.
                var visionNote = noteMap.getClosestAvailableNote(pose.getCurrentPose2d(), false);
                if (visionNote == null) {
                    currentScoringSubGoal = ScoringSubGoals.IngestNoteBlindTerminalApproach;
                    return;
                }
                if (pose.getCurrentPose2d().getTranslation().getDistance(
                        visionNote.toPose2d().getTranslation()) > vision.getMaxNoteSearchingDistanceForSpikeNotes()) {
                    currentScoringSubGoal = ScoringSubGoals.IngestNoteBlindTerminalApproach;
                    return;
                }

                // We found a vision note nearby.
                currentScoringSubGoal = ScoringSubGoals.IngestNoteVisionTerminalApproach;
                setTerminatingPoint(visionNote.toPose2d());
                setSpecialAimTarget(visionNote.toPose2d());
            }
        }
    }

    private void checkForMaskedShotsBecomingAvailable() {
        // check to see if the podium note has been collected (it will be marked Unavailable).
        // If so, check our alliance, and restore the podium shot.
        checkForMaskedShotsOnSpecificAllianceBecomingAvailable(DriverStation.Alliance.Blue);
        checkForMaskedShotsOnSpecificAllianceBecomingAvailable(DriverStation.Alliance.Red);
    }

    private void checkForMaskedShotsOnSpecificAllianceBecomingAvailable(DriverStation.Alliance alliance) {
        // Shots blocked by bottom spike
        checkForAllianceSpecificScoringLocationBecomingAvailabileDueToNoteCollection(
                PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation, PointOfInterest.SpikeBottom, alliance);

        // Shots blocked by middle spike
        checkForAllianceSpecificScoringLocationBecomingAvailabileDueToNoteCollection(
                PointOfInterest.MiddleSpikeScoringLocation, PointOfInterest.SpikeMiddle, alliance);

        // Shots blocked ty top spike
        checkForAllianceSpecificScoringLocationBecomingAvailabileDueToNoteCollection(
                PointOfInterest.TopSpikeCloserToSpeakerScoringLocation, PointOfInterest.SpikeTop, alliance);
    }

    private void checkForAllianceSpecificScoringLocationBecomingAvailabileDueToNoteCollection(
            PointOfInterest scoringLocation, PointOfInterest note, DriverStation.Alliance alliance) {
        if (scoringLocationMap.get(scoringLocation, alliance).getAvailability() == Availability.MaskedByNote
                && DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == alliance
                && noteMap.get(note, alliance).getAvailability() == Availability.Unavailable) {
            scoringLocationMap.get(scoringLocation, alliance).setAvailability(Availability.Available);
            field.getNode(scoringLocation.getName(alliance)).restoreWeights();
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
        double angularError = Math.abs(pose.getAngularErrorToTranslation2dInDegrees(specialAimTarget.getTranslation(), new Rotation2d()));
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

    public Trajectory getTrajectoryRepresentationOfGraph() {
        List<Pose2dNode> eulerianPathInNodes = field.getListOfConnectedNodes();

        var wpiStates = new ArrayList<edu.wpi.first.math.trajectory.Trajectory.State>();
        for (Pose2dNode node : eulerianPathInNodes) {
            var state = new edu.wpi.first.math.trajectory.Trajectory.State();
            state.poseMeters = node.getPose();
            wpiStates.add(state);
        }
        return new Trajectory(wpiStates);
    }

    public PointOfInterest getNearestScoringLocation() {
        return scoringLocationMap.getClosest(pose.getCurrentPose2d().getTranslation(),
                Availability.Available).getPointOfInterest();
    }

    public Pose2d getNearestScoringLocationPose() {
        return scoringLocationMap.getClosest(pose.getCurrentPose2d().getTranslation(),
                Availability.Available).getPointOfInterest().getLocation();
    }
}
