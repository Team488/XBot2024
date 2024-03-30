package competition.subsystems.flipper.commands;

import competition.subsystems.flipper.FlipperSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class CalibrateFlipperCommand extends BaseCommand {

    FlipperSubsystem flipper;
    double startTime;
    final DoubleProperty servoToMaxExecutionTime;

    @Inject
    CalibrateFlipperCommand(FlipperSubsystem flipper, PropertyFactory pf) {
        this.addRequirements(flipper);
        this.flipper = flipper;
        servoToMaxExecutionTime = pf.createPersistentProperty("ServoToMaxExecutionTime", 2);
    }


    @Override
    public void initialize() {
        flipper.servoToMax();
        startTime = XTimer.getFPGATimestamp();
    }

    @Override
    public void execute() {
        // Basically, go to max position, and slowly go down until the sensor is activated
        if (XTimer.getFPGATimestamp() - startTime > servoToMaxExecutionTime.get()) {
            flipper.servo.set(flipper.servo.get() - 0.01);
        }
    }

    @Override
    public boolean isFinished() {
        // Assuming that the sensor is at the inactive position
        if (flipper.getFlipperSensorActivated()) {
            flipper.flipperOffset.set(flipper.getActivePosition() - flipper.servo.get());
            return true;
        }
        return flipper.servo.get() <= 0;
    }
}
