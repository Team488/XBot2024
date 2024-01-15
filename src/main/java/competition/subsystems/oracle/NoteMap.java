package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;

@Singleton
public class NoteMap {
    private HashMap<String, Note> internalNoteMap;

    public NoteMap() {
        this.internalNoteMap = new HashMap<>();
        initializeNotes();
    }

    private void initializeNotes() {
        addNote(Note.KeyNoteNames.SpikeTop,    new Note(new Pose2d(3.92, 7.0, new Rotation2d())));
        addNote(Note.KeyNoteNames.SpikeMiddle, new Note(new Pose2d(3.92, 5.637, new Rotation2d())));
        addNote(Note.KeyNoteNames.SpikeBottom, new Note(new Pose2d(3.92, 4.250, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine1, new Note(new Pose2d(9.05, 7.450, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine2, new Note(new Pose2d(9.05, 5.855, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine3, new Note(new Pose2d(9.05, 4.258, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine4, new Note(new Pose2d(9.05, 2.645, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine5, new Note(new Pose2d(9.05, 1.057, new Rotation2d())));

        addNote(Note.KeyNoteNames.BlueSource, new Note(new Pose2d(14, 1.2, Rotation2d.fromDegrees(0))));
        addNote(Note.KeyNoteNames.CenterLine5, new Note(new Pose2d(4, 1.2, Rotation2d.fromDegrees(180))));
    }

    public void addNote(String key, Note note) {
        this.internalNoteMap.put(key.toString(), note);
    }

    private void addNote(Note.KeyNoteNames key, Note note) {
        this.internalNoteMap.put(key.toString(), note);
    }

    public Note getNote(String key) {
        return this.internalNoteMap.get(key);
    }

    public boolean removeNote(String key) {
        if (this.internalNoteMap.containsKey(key)) {
            this.internalNoteMap.remove(key);
            return true;
        }
        return false;
    }

    public Note getClosestNote(Translation2d point, double withinDistanceMeters, Note.NoteAvailability... availabilities) {
        Note closestNote = null;
        double closestDistance = Double.MAX_VALUE;

        for (Note note : this.internalNoteMap.values()) {
            if (availabilities.length == 0 || Arrays.asList(availabilities).contains(note.getAvailability())) {
                double distance = note.getLocation().getTranslation().getDistance(point);
                if (distance < closestDistance && distance < withinDistanceMeters) {
                    closestDistance = distance;
                    closestNote = note;
                }
            }
        }

        return closestNote;
    }

    public Note getClosestNote(Translation2d point, Note.NoteAvailability... availabilities) {
        return getClosestNote(point, Double.MAX_VALUE, availabilities);
    }
}
