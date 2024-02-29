package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;

public class Note implements ReservableLocation {

    public enum KeyNoteNames{
        BlueSpikeTop,
        BlueSpikeMiddle,
        BlueSpikeBottom,
        RedSpikeTop,
        RedSpikeMiddle,
        RedSpikeBottom,
        CenterLine1,
        CenterLine2,
        CenterLine3,
        CenterLine4,
        CenterLine5
    }

    private int priority;

    private Availability availability;

    private Pose2d location;

    public Note(Pose2d location) {
        this.priority = -1;
        this.availability = Availability.Available;
        this.location = location;
    }

    public Note(Pose2d location, int priority, Availability availability) {
        this.priority = priority;
        this.availability = availability;
        this.location = location;
    }

    public int getPriority() {
        return priority;
    }

    public Availability getAvailability() {
        return availability;
    }

    public Pose2d getLocation() {
        return location;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public void setLocation(Pose2d location) {
        this.location = location;
    }
}
