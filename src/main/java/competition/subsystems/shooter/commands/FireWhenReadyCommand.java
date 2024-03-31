package competition.subsystems.shooter.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class FireWhenReadyCommand extends BaseSetpointCommand {
    final ShooterWheelSubsystem wheel;
    final ArmSubsystem arm;
    final CollectorSubsystem collector;

    @Inject
    public FireWhenReadyCommand(ShooterWheelSubsystem wheel, ArmSubsystem arm, CollectorSubsystem collector,
                                PropertyFactory pf) {
        super(collector);
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
        if (wheel.isReadyToFire() && arm.isMaintainerAtGoal()) {
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
