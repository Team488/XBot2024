package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;

public class Note {

    public enum NoteAvailability {
        Available,
        ReservedByOthersInAuto,
        SuggestedByVision,
        SuggestedByDriver,
        Unavailable
    }

    public enum KeyNoteNames{
        SpikeTop,
        SpikeMiddle,
        SpikeBottom,
        CenterLine1,
        CenterLine2,
        CenterLine3,
        CenterLine4,
        CenterLine5,
        BlueSource,
        RedSource
    }

    private int priority;

    private NoteAvailability availability;

    private Pose2d location;

    public Note(Pose2d location) {
        this.priority = -1;
        this.availability = NoteAvailability.Available;
        this.location = location;
    }

    public Note(Pose2d location, int priority, NoteAvailability availability) {
        this.priority = priority;
        this.availability = availability;
        this.location = location;
    }

    public int getPriority() {
        return priority;
    }

    public NoteAvailability getAvailability() {
        return availability;
    }

    public Pose2d getLocation() {
        return location;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setAvailability(NoteAvailability availability) {
        this.availability = availability;
    }

    public void setLocation(Pose2d location) {
        this.location = location;
    }
}
