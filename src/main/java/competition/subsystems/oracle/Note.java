package competition.subsystems.oracle;

import competition.subsystems.pose.PointOfInterest;
import edu.wpi.first.math.geometry.Pose2d;

public class Note implements ReservableLocation {

    private int priority;

    private Availability availability;

    private Pose2d location;

    private PointOfInterest pointOfInterest;

    public Note(Pose2d location) {
        this.priority = -1;
        this.availability = Availability.Available;
        this.location = location;
    }

    public Note(Pose2d location, PointOfInterest pointOfInterest) {
        this.priority = -1;
        this.availability = Availability.Available;
        this.location = location;
        this.pointOfInterest = pointOfInterest;
    }

    public Note(Pose2d location, int priority, Availability availability, PointOfInterest pointOfInterest) {
        this.priority = priority;
        this.availability = availability;
        this.location = location;
        this.pointOfInterest = pointOfInterest;
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

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }
}
