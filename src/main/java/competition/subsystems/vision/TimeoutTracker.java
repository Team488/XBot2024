package competition.subsystems.vision;

import xbot.common.controls.sensors.XTimer;

import java.util.function.Supplier;

public class TimeoutTracker {

    private Supplier<Double> durationSupplier;
    private double startTime = Double.MAX_VALUE;

    public TimeoutTracker(Supplier<Double> durationSupplier) {
        this.durationSupplier = durationSupplier;
    }

    public void reset() {
        startTime = Double.MAX_VALUE;
    }

    public void start() {
        startTime = XTimer.getFPGATimestamp();
    }

    public double getTimeRemaining() {
        return startTime + durationSupplier.get() - XTimer.getFPGATimestamp();
    }

    public boolean getTimedOut() {
        return getTimeRemaining() < 0;
    }
}
