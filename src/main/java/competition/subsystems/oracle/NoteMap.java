package competition.subsystems.oracle;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;

@Singleton
public class NoteMap extends ReservableLocationMap<Note> {

    public NoteMap() {
        initializeNotes();
    }

    private void initializeNotes() {
        add(Note.KeyNoteNames.BlueSpikeTop,    new Note(PoseSubsystem.SpikeTop));
        add(Note.KeyNoteNames.BlueSpikeMiddle, new Note(PoseSubsystem.SpikeMiddle));
        add(Note.KeyNoteNames.BlueSpikeBottom, new Note(PoseSubsystem.SpikeBottom, 1, Availability.AgainstObstacle));
        add(Note.KeyNoteNames.CenterLine1, new Note(PoseSubsystem.CenterLine1));
        add(Note.KeyNoteNames.CenterLine2, new Note(PoseSubsystem.CenterLine2));
        add(Note.KeyNoteNames.CenterLine3, new Note(PoseSubsystem.CenterLine3));
        add(Note.KeyNoteNames.CenterLine4, new Note(PoseSubsystem.CenterLine4));
        add(Note.KeyNoteNames.CenterLine5, new Note(PoseSubsystem.CenterLine5));
        add(Note.KeyNoteNames.RedSpikeTop,    new Note(BasePoseSubsystem.convertBluetoRed(PoseSubsystem.SpikeTop)));
        add(Note.KeyNoteNames.RedSpikeMiddle, new Note(BasePoseSubsystem.convertBluetoRed(PoseSubsystem.SpikeMiddle)));
        add(Note.KeyNoteNames.RedSpikeBottom, new Note(BasePoseSubsystem.convertBluetoRed(PoseSubsystem.SpikeBottom),1, Availability.AgainstObstacle));
    }

    public void markAllianceNotesAsUnavailable(DriverStation.Alliance alliance) {
        if (alliance == DriverStation.Alliance.Red) {
            get(Note.KeyNoteNames.RedSpikeTop).setAvailability(Availability.Unavailable);
            get(Note.KeyNoteNames.RedSpikeMiddle).setAvailability(Availability.Unavailable);
            get(Note.KeyNoteNames.RedSpikeBottom).setAvailability(Availability.Unavailable);
        } else {
            get(Note.KeyNoteNames.BlueSpikeTop).setAvailability(Availability.Unavailable);
            get(Note.KeyNoteNames.BlueSpikeMiddle).setAvailability(Availability.Unavailable);
            get(Note.KeyNoteNames.BlueSpikeBottom).setAvailability(Availability.Unavailable);
        }
    }

    private void add(Note.KeyNoteNames key, Note note) {
            add(key.toString(), note);
    }

    public Note get(Note.KeyNoteNames key) {
        return get(key.toString());
    }

    public Pose3d[] getAllKnownNotes() {
        Pose3d[] notes = new Pose3d[internalMap.size()];
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
        return notes;
    }
}
