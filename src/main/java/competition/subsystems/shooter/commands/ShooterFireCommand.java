package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class ShooterFireCommand extends BaseCommand {

    final ShooterWheelSubsystem wheel;
    final ArmSubsystem arm;
    final CollectorSubsystem collector;
    final DoubleProperty shooterPower;
    final OperatorInterface oi;

    @Inject
    public ShooterFireCommand(OperatorInterface oi, ShooterWheelSubsystem wheel, ArmSubsystem arm, CollectorSubsystem collector, PropertyFactory pf) {
        this.oi = oi;


        this.wheel = wheel;
        shooterPower = pf.createPersistentProperty("Shooter Power", 1);
        this.collector = collector;
        this.arm = arm;
        this.addRequirements(this.wheel);
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
