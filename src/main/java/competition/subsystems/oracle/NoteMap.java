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
        addNote(Note.KeyNoteNames.SpikeTop,    new Note(new Pose2d(2.94, 7.0, new Rotation2d())));
        addNote(Note.KeyNoteNames.SpikeMiddle, new Note(new Pose2d(2.94, 5.483, new Rotation2d())));
        addNote(Note.KeyNoteNames.SpikeBottom, new Note(new Pose2d(2.94, 4.125, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine1, new Note(new Pose2d(8.29, 7.436, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine2, new Note(new Pose2d(8.29, 5.772, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine3, new Note(new Pose2d(8.29, 4.108, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine4, new Note(new Pose2d(8.29, 2.493, new Rotation2d())));
        addNote(Note.KeyNoteNames.CenterLine5, new Note(new Pose2d(8.29, 0.804, new Rotation2d())));
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
