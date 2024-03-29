package competition.subsystems.flipper.commands;

import competition.subsystems.flipper.FlipperSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class CalibrateFlipperCommand extends BaseCommand {

    FlipperSubsystem flipper;
    boolean forcedFinish;
    int executionLoop;

    @Inject
    CalibrateFlipperCommand(FlipperSubsystem flipper) {
        this.addRequirements(flipper);
        this.flipper = flipper;
    }


    @Override
    public void initialize() {
        forcedFinish = false;
        executionLoop = 0;
        flipper.servoToMax();
    }

    @Override
    public void execute() {
        // Basically, go to max position, and slowly go down until the sensor is activated
        executionLoop++;

        // Give 2 seconds for servo to go to max position
        if (executionLoop > 100 && flipper.servo.get() > 0) {
            // Slowly increment down the servo position
            flipper.servo.set(flipper.servo.get() - 0.01);
        } else {
            forcedFinish = true;
        }
    }

    @Override
    public boolean isFinished() {
        // Assuming that the sensor is at the inactive position
        if (flipper.getFlipperSensorActivated()) {
            flipper.flipperOffset.set(flipper.activePosition.get() - flipper.servo.get());
            return true;
        }
        return forcedFinish;
    }
}
