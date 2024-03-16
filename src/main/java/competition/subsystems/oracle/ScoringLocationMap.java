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
        addForBothAlliances(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation);
        addForBothAlliances(PointOfInterest.MiddleSpikeScoringLocation);
        addForBothAlliances(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation);
        addForBothAlliances(PointOfInterest.OneRobotAwayFromCenterSubwooferScoringLocation);
    }

    private void addForBothAlliances(PointOfInterest pointOfInterest) {
        add(pointOfInterest.getBlueName(), new ScoringLocation(pointOfInterest.getBlueLocation(), Availability.Available, pointOfInterest));
        add(pointOfInterest.getRedName(), new ScoringLocation(pointOfInterest.getRedLocation(), Availability.Available, pointOfInterest));
    }

    public void markAllianceScoringLocationsWithAvailability(DriverStation.Alliance alliance, Availability availability) {
        get(PointOfInterest.SubwooferTopScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.SubwooferMiddleScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.SubwooferBottomScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.PodiumScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.AmpFarScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.MiddleSpikeScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation, alliance).setAvailability(availability);
        get(PointOfInterest.OneRobotAwayFromCenterSubwooferScoringLocation, alliance).setAvailability(availability);
    }

    public ScoringLocation get(PointOfInterest pointOfInterest, DriverStation.Alliance alliance) {
        return get(pointOfInterest.getName(alliance));
    }
}
