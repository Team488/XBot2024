package competition.subsystems.oracle;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
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
        add(ScoringLocation.WellKnownScoringLocations.SubwooferTopBlue, PoseSubsystem.BlueSubwooferTopScoringLocation);
        add(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleBlue, PoseSubsystem.BlueSubwooferCentralScoringLocation);
        add(ScoringLocation.WellKnownScoringLocations.SubwooferBottomBlue, PoseSubsystem.BlueSubwooferBottomScoringLocation);

        add(ScoringLocation.WellKnownScoringLocations.PodiumBlue, PoseSubsystem.BluePodiumScoringLocation);
        add(ScoringLocation.WellKnownScoringLocations.AmpZoneFarBlue, PoseSubsystem.BlueTopAmpScoringLocation);
    }

    private void initializeRedScoringLocations() {
        add(ScoringLocation.WellKnownScoringLocations.SubwooferTopRed, PoseSubsystem.convertBluetoRed(PoseSubsystem.BlueSubwooferTopScoringLocation));
        add(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleRed, PoseSubsystem.convertBluetoRed(PoseSubsystem.BlueSubwooferCentralScoringLocation));
        add(ScoringLocation.WellKnownScoringLocations.SubwooferBottomRed, PoseSubsystem.convertBluetoRed(PoseSubsystem.BlueSubwooferBottomScoringLocation));

        add(ScoringLocation.WellKnownScoringLocations.PodiumRed, PoseSubsystem.convertBluetoRed(PoseSubsystem.BluePodiumScoringLocation));
        add(ScoringLocation.WellKnownScoringLocations.AmpZoneFarRed, PoseSubsystem.convertBluetoRed(PoseSubsystem.BlueTopAmpScoringLocation));
    }

    public void markAllianceScoringLocationsAsUnavailable(DriverStation.Alliance alliance) {
        if (alliance == DriverStation.Alliance.Red) {
            get(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleRed).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferTopRed).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferBottomRed).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.PodiumRed).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.AmpZoneFarRed).setAvailability(Availability.Unavailable);
        } else {
            get(ScoringLocation.WellKnownScoringLocations.SubwooferMiddleBlue).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferTopBlue).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.SubwooferBottomBlue).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.PodiumBlue).setAvailability(Availability.Unavailable);
            get(ScoringLocation.WellKnownScoringLocations.AmpZoneFarBlue).setAvailability(Availability.Unavailable);
        }
    }

    private void add(ScoringLocation.WellKnownScoringLocations key, Pose2d location) {
        add(key.toString(), new ScoringLocation(location, Availability.Available, key));
    }

    public ScoringLocation get(ScoringLocation.WellKnownScoringLocations key) {
        return get(key.toString());
    }
}
