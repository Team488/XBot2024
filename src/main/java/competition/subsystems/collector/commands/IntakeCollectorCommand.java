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
    private final DoubleProperty intensity;
    private boolean isToggledOnce = false;
    @Inject
    public IntakeCollectorCommand(CollectorSubsystem collector, OperatorInterface oi, PropertyFactory pf) {
        this.collector = collector;
        this.oi = oi;
        addRequirements(collector);
        pf.setPrefix(this);
        intensity = pf.createPersistentProperty("intensity",1);
    }

    @Override
    public void initialize() {
        collector.resetCollectionState();
        log.info("Initializing");
        isToggledOnce = false;
    }

    @Override
    public void execute() {
        collector.intake();
        if(collector.getGamePieceInControl()) {
            isToggledOnce = true;
            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
        }

        if (collector.confidentlyHasControlOfNote()) {

            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
        } else if (isToggledOnce) {
            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity.get(), 0.7);
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(0, 0.7);
        oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(0, 0.7);
        oi.driverGamepad.getRumbleManager().rumbleGamepad(0, 0.7);

        collector.stop();

    }
}