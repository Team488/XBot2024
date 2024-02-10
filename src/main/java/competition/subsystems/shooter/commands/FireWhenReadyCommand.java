package competition.subsystems.shooter.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class FireWhenReadyCommand extends BaseCommand {
    final ShooterWheelSubsystem wheel;
    final ArmSubsystem arm;
    final CollectorSubsystem collector;

    DoubleProperty waitTimeAfterFiring;
    boolean hasFired;
    double timeWhenFired;

    @Inject
    public FireWhenReadyCommand(ShooterWheelSubsystem wheel, ArmSubsystem arm, CollectorSubsystem collector,
                                PropertyFactory pf) {
        this.wheel = wheel;
        this.arm = arm;
        this.collector = collector;

        this.waitTimeAfterFiring = pf.createPersistentProperty("WaitTimeAfterFiring", 0.5);
        this.hasFired = false;

        pf.setPrefix(this);
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
        this.hasFired = false;
    }

    @Override
    public void execute() {
        /* WE CANNOT CHECK IF CURRENT VALUE OF WHEEL AND TARGET VALUE ARE EXACTLY THE SAME, THAT IS WHY WE
        HAVE THE TOLERANCE PROPERTIES IN THE FIRST PLACE

        RUNS 50 TIMES A SECOND
        */
        if (hasFired || (wheel.isMaintainerAtGoal() && arm.isMaintainerAtGoal())) {
            collector.fire();

            if (!hasFired) {
                hasFired = true;
                timeWhenFired = XTimer.getFPGATimestamp();
            }
        }
    }

    @Override
    public boolean isFinished() {
        return hasFired && XTimer.getFPGATimestamp() + waitTimeAfterFiring.get() >= timeWhenFired;
    }
}
