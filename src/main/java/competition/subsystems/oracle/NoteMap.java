package competition.subsystems.oracle;

import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.controls.sensors.XTimer;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class NoteMap extends ReservableLocationMap<Note> {

    private final List<VisionSourceNote> visionSourceNotes;

    public static final int MAX_VISION_SOURCE_NOTE_COUNT = 10;

    public NoteMap() {
        initializeNotes();
        visionSourceNotes = new ArrayList<>();
    }

    private void initializeNotes() {
        addForBothAlliances(PointOfInterest.SpikeTop);
        addForBothAlliances(PointOfInterest.SpikeMiddle);
        addForBothAlliances(PointOfInterest.SpikeBottom);
        add(PointOfInterest.CenterLine1);
        add(PointOfInterest.CenterLine2);
        add(PointOfInterest.CenterLine3);
        add(PointOfInterest.CenterLine4);
        add(PointOfInterest.CenterLine5);
    }

    public void markAllianceNotesAsUnavailable(DriverStation.Alliance alliance) {
        get(PointOfInterest.SpikeTop, alliance).setAvailability(Availability.Unavailable);
        get(PointOfInterest.SpikeMiddle, alliance).setAvailability(Availability.Unavailable);
        get(PointOfInterest.SpikeBottom, alliance).setAvailability(Availability.Unavailable);
    }

    private void add(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
            add(pointOfInterest.getName(alliance), new Note(
                    pointOfInterest.getLocation(alliance), pointOfInterest));

    }

    private void add(PointOfInterest pointOfInterest) {
        add(pointOfInterest.getName(), new Note(pointOfInterest.getLocation(), pointOfInterest));
    }

    private void addForBothAlliances(PointOfInterest pointOfInterest) {
        add(pointOfInterest.getBlueName(), new Note(pointOfInterest.getBlueLocation(), pointOfInterest));
        add(pointOfInterest.getRedName(), new Note(pointOfInterest.getRedLocation(), pointOfInterest));
    }

    public void addVisionNote(Pose2d location) {
        if (visionSourceNotes.size() >= MAX_VISION_SOURCE_NOTE_COUNT) {
            visionSourceNotes.remove(0);
        }
        visionSourceNotes.add(new VisionSourceNote(new Note(location), XTimer.getFPGATimestamp()));
    }

    public void clearStaleVisionNotes(double maxAgeInSeconds) {
        double currentTime = XTimer.getFPGATimestamp();
        visionSourceNotes.removeIf(note -> currentTime - note.getTimestamp() > maxAgeInSeconds);
    }

    public boolean hasVisionNotes() {
        return !visionSourceNotes.isEmpty();
    }

    public void clearVisionNotes() {
        visionSourceNotes.clear();
    }

    public void markSpikeNotesUnavailable() {
        for (var note: this.internalMap.values()) {
            note.setAvailability(Availability.Unavailable);
        }
    }

    public Note get(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        if (pointOfInterest.isUnique()) {
            return get(pointOfInterest.getName());
        } else {
            return get(pointOfInterest.getName(alliance));
        }
    }

    public Pose3d getClosestAvailableNote(Pose2d referencePoint, boolean includeStaticNotes) {
        double closestDistance = Double.MAX_VALUE;
        Note closestNote = null;
        ArrayList<Note> allNotes;
        if (includeStaticNotes) {
            allNotes = new ArrayList<Note>(this.internalMap.values());
        } else {
            allNotes = new ArrayList<Note>();
        }
        allNotes.addAll(visionSourceNotes.stream().map(VisionSourceNote::getNote).toList());
        for (Note note : allNotes) {
            if (note.getAvailability() == Availability.Available) {
                double distance = note.getLocation().getTranslation().getDistance(referencePoint.getTranslation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestNote = note;
                }
            }
        }
        if (closestNote != null) {
            return new Pose3d(new Translation3d(
                    closestNote.getLocation().getX(),
                    closestNote.getLocation().getY(),
                    0.025),
                    new Rotation3d(0,0,0));
        }
        return null;
    }

    public Pose3d[] getAllKnownNotes() {
        Pose3d[] notes = new Pose3d[internalMap.size() + visionSourceNotes.size()];
        int i = 0;
        for (Note note : this.internalMap.values()) {

            double noteZ = 0.025;
            if (note.getAvailability() != Availability.Available && note.getAvailability() != Availability.AgainstObstacle) {
                noteZ = -3.0;
            }

            notes[i] = new Pose3d(new Translation3d(
                    note.getLocation().getX(),
                    note.getLocation().getY(),
                    noteZ),
                    new Rotation3d(0,0,0));
            i++;
        }

        for (VisionSourceNote note : visionSourceNotes) {
            notes[i] = new Pose3d(new Translation3d(
                    note.getNote().getLocation().getX(),
                    note.getNote().getLocation().getY(),
                    0.025),
                    new Rotation3d(0,0,0));
            i++;
        }

        return notes;
    }
}
