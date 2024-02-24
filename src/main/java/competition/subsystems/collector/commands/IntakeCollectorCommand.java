package competition.subsystems.collector.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class IntakeCollectorCommand extends BaseCommand {
    CollectorSubsystem collector;
    final OperatorInterface oi;
    double intensity = 0.1;

    @Inject
    public IntakeCollectorCommand(CollectorSubsystem collector, OperatorInterface oi) {
        this.collector = collector;
        this.oi = oi;
        addRequirements(collector);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        collector.intake();
        if (collector.getGamePieceReady()) {
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity, 0.1);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity, 0.1);
        }
    }
}