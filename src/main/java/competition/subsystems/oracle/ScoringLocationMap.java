package competition.subsystems.oracle;

import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;

public class ScoringLocationMap extends ReservableLocationMap<ScoringLocation> {

    public ScoringLocationMap() {
        initializeScoringLocations();
    }

    private void initializeScoringLocations() {
        addForBothAlliances(PointOfInterest.SubwooferTopScoringLocation);
        addForBothAlliances(PointOfInterest.SubwooferMiddleScoringLocation);
        addForBothAlliances(PointOfInterest.SubwooferBottomScoringLocation);
        addForBothAlliances(PointOfInterest.PodiumScoringLocation);
        addForBothAlliances(PointOfInterest.AmpFarScoringLocation);
    }

    private void addForBothAlliances(PointOfInterest pointOfInterest) {
        add(pointOfInterest.getBlueName(), new ScoringLocation(pointOfInterest.getBlueLocation(), Availability.Available, pointOfInterest));
        add(pointOfInterest.getRedName(), new ScoringLocation(pointOfInterest.getRedLocation(), Availability.Available, pointOfInterest));
    }

    public void markAllianceScoringLocationsAsUnavailable(DriverStation.Alliance alliance) {
        get(PointOfInterest.SubwooferTopScoringLocation, alliance).setAvailability(Availability.Unavailable);
        get(PointOfInterest.SubwooferMiddleScoringLocation, alliance).setAvailability(Availability.Unavailable);
        get(PointOfInterest.SubwooferBottomScoringLocation, alliance).setAvailability(Availability.Unavailable);
        get(PointOfInterest.PodiumScoringLocation, alliance).setAvailability(Availability.Unavailable);
        get(PointOfInterest.AmpFarScoringLocation, alliance).setAvailability(Availability.Unavailable);
    }

    public ScoringLocation get(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        return get(pointOfInterest.getName(alliance));
    }
}
