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

    @Inject
    public FireWhenReadyCommand(ShooterWheelSubsystem wheel, ArmSubsystem arm, CollectorSubsystem collector,
                                PropertyFactory pf) {
        addRequirements(collector);
        this.wheel = wheel;
        this.arm = arm;
        this.collector = collector;
        pf.setPrefix(this);
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
    }

    @Override
    public void execute() {
        /* WE CANNOT CHECK IF CURRENT VALUE OF WHEEL AND TARGET VALUE ARE EXACTLY THE SAME, THAT IS WHY WE
        HAVE THE TOLERANCE PROPERTIES IN THE FIRST PLACE

        RUNS 50 TIMES A SECOND
        */
        if (collector.getIntakeState() == CollectorSubsystem.IntakeState.FIRING || (wheel.isMaintainerAtGoal() && arm.isMaintainerAtGoal())) {
            collector.fire();
        }
    }

    @Override
    public boolean isFinished() {
        return collector.confidentlyHasFiredNote();
    }

    @Override
    public void end(boolean interrupted) {
        log.info("Ending");
        super.end(interrupted);
    }
}
