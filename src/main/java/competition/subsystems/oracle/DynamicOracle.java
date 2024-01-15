package competition.subsystems.oracle;

import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    HighLevelGoal currentHighLevelGoal;
    boolean firstRunInNewGoal = true;

    PoseSubsystem pose;

    @Inject
    public DynamicOracle(NoteCollectionInfoSource noteCollectionInfoSource, NoteFiringInfoSource noteFiringInfoSource,
                         NoteMap noteMap, PoseSubsystem pose) {
        this.noteCollectionInfoSource = noteCollectionInfoSource;
        this.noteFiringInfoSource = noteFiringInfoSource;
        this.noteMap = noteMap;
        this.pose = pose;

        this.currentHighLevelGoal = HighLevelGoal.ScoreInSpeaker;
    }

    public void overrideGoal(HighLevelGoal goal) {
        if (goal != currentHighLevelGoal) {
            firstRunInNewGoal = true;
        }
        this.currentHighLevelGoal = goal;
    }

    @Override
    public void periodic() {

        switch (currentHighLevelGoal) {
            case ScoreInAmp: // For now keeping things simple
            case ScoreInSpeaker:
                if (firstRunInNewGoal) {
                    // Choose a good speaker scoring location
                    // Publish a route from current position to that location
                }
                // If we've launched our note, time to get another one
                if (noteFiringInfoSource.confidentlyHasFiredNote()) {
                    currentHighLevelGoal = HighLevelGoal.CollectNote;
                    firstRunInNewGoal = true;
                    break;
                }
                break;
            case CollectNote:
                if (firstRunInNewGoal) {
                    // Choose a good note collection location
                    setTargetNote(noteMap.getClosestNote(pose.getCurrentPose2d().getTranslation(),
                            Note.NoteAvailability.Available,
                            Note.NoteAvailability.SuggestedByDriver,
                            Note.NoteAvailability.SuggestedByVision));
                    // Publish a route from current position to that location
                    setTerminatingPoint(new XbotSwervePoint(getTargetNote().getLocation(), 10));
                }

                if (noteCollectionInfoSource.confidentlyHasControlOfNote()) {
                    // Mark the nearest note as being unavailable, if we are anywhere near it
                    Note nearestNote = noteMap.getClosestNote(pose.getCurrentPose2d().getTranslation(), 1.0);
                    nearestNote.setAvailability(Note.NoteAvailability.Unavailable);


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
    }

    Note targetNote;
    private void setTargetNote(Note note) {
        this.targetNote = note;
    }

    private Note getTargetNote() {
        return this.targetNote;
    }

    XbotSwervePoint terminatingPoint;
    private void setTerminatingPoint(XbotSwervePoint point) {
        this.terminatingPoint = point;
    }

    private double getEstimatedSecondsUntilScoringRequired() {
        return 0;
    }
}
