package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;

public class ScoringLocation implements ReservableLocation {

    public enum WellKnownScoringLocations {
        SubwooferTopBlue,
        SubwooferMiddleBlue,
        SubwooferBottomBlue,
        SubwooferTopRed,
        SubwooferMiddleRed,
        SubwooferBottomRed,
        PodiumBlue,
        AmpZoneFarBlue,
        PodiumRed,
        AmpZoneFarRed
    }

    private Pose2d location;
    private Availability availability;
    private WellKnownScoringLocations locationName;

    public ScoringLocation(Pose2d location, Availability availability, WellKnownScoringLocations locationName) {
        this.location = location;
        this.availability = availability;
        this.locationName = locationName;
    }

    public Pose2d getLocation() {
        return location;
    }

    public Availability getAvailability() {
        return availability;
    }

    public WellKnownScoringLocations getWellKnownLocation() {
        return locationName;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }
}
