package competition.subsystems.oracle;

import competition.subsystems.pose.PointOfInterest;
import edu.wpi.first.math.geometry.Pose2d;

public class ScoringLocation implements ReservableLocation {

    private Pose2d location;
    private Availability availability;
    private PointOfInterest pointOfInterest;

    public ScoringLocation(Pose2d location, Availability availability, PointOfInterest pointOfInterest) {
        this.location = location;
        this.availability = availability;
        this.pointOfInterest = pointOfInterest;
    }

    public Pose2d getLocation() {
        return location;
    }

    public Availability getAvailability() {
        return availability;
    }

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }
}
