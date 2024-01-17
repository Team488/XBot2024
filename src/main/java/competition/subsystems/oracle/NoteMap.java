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
        addNote(Note.KeyNoteNames.SpikeTop,    new Note(new Pose2d(2.8956, 7.0012, new Rotation2d())));
        addNote(Note.KeyNoteNames.SpikeMiddle, new Note(new Pose2d(2.8956, 5.5534, new Rotation2d())));
        addNote(Note.KeyNoteNames.SpikeBottom, new Note(new Pose2d(2.8956, 4.1056, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine1, new Note(new Pose2d(8.2956, 7.4584, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine2, new Note(new Pose2d(8.2956, 5.7820, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine3, new Note(new Pose2d(8.2956, 4.1056, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine4, new Note(new Pose2d(8.2956, 2.4292, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine5, new Note(new Pose2d(8.2956, 0.7528, new Rotation2d())));
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

    public Note getNote(Note.KeyNoteNames key) {
        return this.internalNoteMap.get(key.toString());
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
