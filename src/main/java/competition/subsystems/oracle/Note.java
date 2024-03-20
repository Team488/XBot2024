package competition.subsystems.oracle;

import competition.subsystems.pose.PointOfInterest;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

public class Note implements ReservableLocation {

    private int priority;

    private Availability availability;

    private UnavailableReason unavailableReason;

    private final DataSource dataSource;

    private final Pose2d location;

    private final PointOfInterest pointOfInterest;

    public Note(Pose2d location, DataSource dataSource) {
        this(location, Availability.Available, dataSource);
    }

    public Note(Pose2d location, Availability availability, DataSource dataSource) {
        this.priority = -1;
        this.availability = availability;
        this.location = location;
        this.dataSource = dataSource;
        this.pointOfInterest = null;
    }

    public Note(Pose2d location, PointOfInterest pointOfInterest) {
        this(location, -1, Availability.Available, pointOfInterest);
    }

    public Note(Pose2d location, int priority, Availability availability, PointOfInterest pointOfInterest) {
        this.priority = priority;
        this.availability = availability;
        this.location = location;
        this.pointOfInterest = pointOfInterest;
        this.dataSource = DataSource.Static;
    }

    public int getPriority() {
        return priority;
    }

    public Availability getAvailability() {
        return availability;
    }

    public UnavailableReason getUnavailableReason() {
        return unavailableReason;
    }

    public Pose2d getLocation() {
        return location;
    }

    public Pose3d get3dLocation() {
        return new Pose3d(new Translation3d(location.getX(), location.getY(), 0.25), new Rotation3d());
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setAvailable() {
        this.availability = Availability.Available;
        this.unavailableReason = null;
    }

    public void setUnavailable(UnavailableReason reason) {
        this.availability = Availability.Unavailable;
        this.unavailableReason = reason;
    }

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }
}
