package competition.subsystems.collector.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;


public class IntakeCollectorCommand extends BaseCommand {
    CollectorSubsystem collector;
    final OperatorInterface oi;
    private final DoubleProperty higherIntensity;
    private final DoubleProperty lowerIntensity;

    @Inject
    public IntakeCollectorCommand(CollectorSubsystem collector, OperatorInterface oi, PropertyFactory pf) {
        addRequirements(collector);
        this.collector = collector;
        this.oi = oi;
        pf.setPrefix(this);
        higherIntensity = pf.createPersistentProperty("higher intensity",1);
        lowerIntensity = pf.createPersistentProperty("lower intensity", 0.2);
    }

    @Override
    public void initialize() {
        collector.resetCollectionState();
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.intake();
        if (collector.confidentlyHasControlOfNote()) {
            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(higherIntensity.get(), 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(higherIntensity.get(), 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(higherIntensity.get(), 0.7);
        } else if (collector.getBeamBreakSensorActivated()) {
            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(lowerIntensity.get(), 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(lowerIntensity.get(), 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(lowerIntensity.get(), 0.7);
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        collector.stop();

    }
}