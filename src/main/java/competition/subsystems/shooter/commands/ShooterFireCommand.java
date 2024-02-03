package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class ShooterFireCommand extends BaseCommand {

    final ShooterWheelSubsystem wheel;
    final ArmSubsystem arm;
    final DoubleProperty shooterPower;
    final OperatorInterface oi;

    @Inject
    public ShooterFireCommand(OperatorInterface oi, ShooterWheelSubsystem wheel, ArmSubsystem arm, PropertyFactory pf) {
        this.oi = oi;

        this.wheel = wheel;
        shooterPower = pf.createPersistentProperty("Shooter Power", 1);

        this.arm = arm;
        this.addRequirements(this.wheel);
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
    }

    @Override
    public void execute() {
        if (wheel.getCurrentValue() == wheel.getTargetValue() && arm.getCurrentValue() == arm.getTargetValue()) {
            wheel.setPower(shooterPower.get());
        } else {
            log.info("Desired RPM and arm position have not been reached yet");
        }
    }
}
