package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;

public class ScoringLocation implements ReservableLocation {

    public enum WellKnownScoringLocations {
        SubwooferTopBlue,
        SubwooferMiddleBlue,
        SubwooferBottomBlue,
        SubwooferTopRed,
        SubwooferMiddleRed,
        SubwooferBottomRed
    }

    private Pose2d location;
    private Availability availability;

    public ScoringLocation(Pose2d location, Availability availability) {
        this.location = location;
        this.availability = availability;
    }

    public Pose2d getLocation() {
        return location;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }
}
