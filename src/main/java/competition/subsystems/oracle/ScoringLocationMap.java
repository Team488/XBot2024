package competition.subsystems.oracle;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj.DriverStation;

public class ScoringLocationMap extends ReservableLocationMap<ScoringLocation> {

    public ScoringLocationMap() {
        initializeScoringLocations();
    }

    private void initializeScoringLocations() {
        initializeBlueScoringLocations();
        initializeRedScoringLocations();
    }

    private void initializeBlueScoringLocations() {
        add(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleBlue, new ScoringLocation(PoseSubsystem.SubwooferCentralScoringLocation, Availability.Available));
        add(ScoringLocation.WellKnownScoringLocations.SubwooferTopBlue, new ScoringLocation(PoseSubsystem.SubwooferTopScoringLocation, Availability.Available));
        add(ScoringLocation.WellKnownScoringLocations.SubwooferBottomBlue, new ScoringLocation(PoseSubsystem.SubwooferBottomScoringLocation, Availability.Available));
    }

    private void initializeRedScoringLocations() {
        add(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleRed, new ScoringLocation(PoseSubsystem.convertBluetoRed(PoseSubsystem.SubwooferCentralScoringLocation), Availability.Available));
        add(ScoringLocation.WellKnownScoringLocations.SubwooferTopRed, new ScoringLocation(PoseSubsystem.convertBluetoRed(PoseSubsystem.SubwooferTopScoringLocation), Availability.Available));
        add(ScoringLocation.WellKnownScoringLocations.SubwooferBottomRed, new ScoringLocation(PoseSubsystem.convertBluetoRed(PoseSubsystem.SubwooferBottomScoringLocation), Availability.Available));
    }

    public void markAllianceScoringLocationsAsUnavailable(DriverStation.Alliance alliance) {
        if (alliance == DriverStation.Alliance.Red) {
            get(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleRed).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferTopRed).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferBottomRed).setAvailability(Availability.Unavailable);
        } else {
            get(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleBlue).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferTopBlue).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferBottomBlue).setAvailability(Availability.Unavailable);
        }
    }

    private void add(ScoringLocation.WellKnownScoringLocations key, ScoringLocation location) {
        add(key.toString(), location);
    }

    public ScoringLocation get(ScoringLocation.WellKnownScoringLocations key) {
        return get(key.toString());
    }
}
