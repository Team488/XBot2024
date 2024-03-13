package competition.subsystems.collector.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class IntakeCollectorCommand extends BaseCommand {
    CollectorSubsystem collector;
    final OperatorInterface oi;
    double intensity = 0.2;
    private boolean isToggledOnce = false;
    @Inject
    public IntakeCollectorCommand(CollectorSubsystem collector, OperatorInterface oi) {
        this.collector = collector;
        this.oi = oi;
        addRequirements(collector);
    }

    @Override
    public void initialize() {
        collector.resetCollectionState();
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.intake();
        if(collector.getGamePieceInControl()) {
            isToggledOnce = true;
            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(intensity, 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity, 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity, 0.7);
        }

        if (collector.confidentlyHasControlOfNote() && !isToggledOnce) {

            oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(intensity, 0.7);
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity, 0.7);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity, 0.7);
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        oi.operatorGamepadAdvanced.getRumbleManager().rumbleGamepad(0, 0.7);
        oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(0, 0.7);
        oi.driverGamepad.getRumbleManager().rumbleGamepad(0, 0.7);

        collector.stop();
        isToggledOnce = false;
    }
}