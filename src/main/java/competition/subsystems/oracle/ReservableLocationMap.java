package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Translation2d;

import java.util.Arrays;
import java.util.HashMap;

public class ReservableLocationMap<T extends ReservableLocation> {

    protected HashMap<String, T> internalMap;

    public ReservableLocationMap() {
        this.internalMap = new HashMap<>();
    }

    public void add(String key, T location) {
        this.internalMap.put(key, location);
    }

    public T get(String key) {
        return this.internalMap.get(key);
    }

    public void remove(String key) {
        this.internalMap.remove(key);
    }

    public T getClosest(Translation2d point, double withinDistanceMeters, Availability... availabilities) {
        T closestLocation = null;
        double closestDistance = Double.MAX_VALUE;

        for (T reservableLocation : this.internalMap.values()) {
            if (availabilities.length == 0 || Arrays.asList(availabilities).contains(reservableLocation.getAvailability())) {
                double distance = reservableLocation.getLocation().getTranslation().getDistance(point);
                if (distance < closestDistance && distance < withinDistanceMeters) {
                    closestDistance = distance;
                    closestLocation = reservableLocation;
                }
            }
        }

        return closestLocation;
    }

    public T getClosest(Translation2d point, Availability... availabilities) {
        return getClosest(point, Double.MAX_VALUE, availabilities);
    }
}
