package competition.subsystems.shooter.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FireWhenReadyCommand extends BaseCommand {
    final ShooterWheelSubsystem wheel;
    final ArmSubsystem arm;
    final CollectorSubsystem collector;

    @Inject
    public FireWhenReadyCommand(ShooterWheelSubsystem wheel, ArmSubsystem arm, CollectorSubsystem collector) {
        this.wheel = wheel;
        this.arm = arm;
        this.collector = collector;
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
        if (wheel.isMaintainerAtGoal() && arm.isMaintainerAtGoal()) {
            collector.fire();
        }
    }
}
