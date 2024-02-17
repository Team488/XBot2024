package competition.subsystems.arm.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.math.MathUtils;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class EmergencyManualArmControlCommand extends BaseCommand {
    ArmSubsystem arm;
    OperatorInterface oi;
    DoubleProperty armPowerFactor;

    @Inject
    public EmergencyManualArmControlCommand(ArmSubsystem arm, OperatorInterface oi, PropertyFactory pf) {
        this.arm = arm;
        this.oi = oi;
        pf.setPrefix(this);
        this.armPowerFactor = pf.createPersistentProperty("Arm Power Factor", 0.25);
        addRequirements(arm);
    }
    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        arm.setPower(MathUtils.deadband(oi.operatorGamepad.getLeftStickY() * armPowerFactor.get(), 0.15));
    }

}
