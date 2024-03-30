package competition.subsystems.oracle;

import competition.subsystems.pose.PointOfInterest;
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
        addForBothAlliances(PointOfInterest.UnderChain);
        addForBothAlliances(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation);
        addForBothAlliances(PointOfInterest.MiddleSpikeScoringLocation);
        addForBothAlliances(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation);
        addForBothAlliances(PointOfInterest.OneRobotAwayFromCenterSubwooferScoringLocation);
    }

    private void addForBothAlliances(PointOfInterest pointOfInterest) {
        add(pointOfInterest.getBlueName(), new ScoringLocation(pointOfInterest.getBlueLocation(), Availability.Available, pointOfInterest));
        add(pointOfInterest.getRedName(), new ScoringLocation(pointOfInterest.getRedLocation(), Availability.Available, pointOfInterest));
    }

    public void markAllianceScoringLocationsAvailable(DriverStation.Alliance alliance) {
        for (var location : getAllianceScoringLocations(alliance)) {
            location.setAvailable();
        }
    }

    public void markAllianceScoringLocationsUnavailable(DriverStation.Alliance alliance, UnavailableReason reason) {
        for (var location : getAllianceScoringLocations(alliance)) {
            location.setUnavailable(reason);
        }
    }

    public ScoringLocation[] getAllianceScoringLocations(DriverStation.Alliance alliance) {
        return new ScoringLocation[]{
            get(PointOfInterest.SubwooferTopScoringLocation, alliance),
            get(PointOfInterest.SubwooferMiddleScoringLocation, alliance),
            get(PointOfInterest.SubwooferBottomScoringLocation, alliance),
            get(PointOfInterest.PodiumScoringLocation, alliance),
            get(PointOfInterest.AmpFarScoringLocation, alliance),
            get(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation, alliance),
            get(PointOfInterest.MiddleSpikeScoringLocation, alliance),
            get(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation, alliance),
            get(PointOfInterest.OneRobotAwayFromCenterSubwooferScoringLocation, alliance),
        };
    }

    public ScoringLocation get(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        return get(pointOfInterest.getName(alliance));
    }
}
