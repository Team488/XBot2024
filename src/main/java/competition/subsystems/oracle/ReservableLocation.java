package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;

public interface ReservableLocation {
    public Pose2d getLocation();

    public Availability getAvailability();

    public UnavailableReason getUnavailableReason();

    public void setAvailable();

    public void setUnavailable(UnavailableReason reason);
}
