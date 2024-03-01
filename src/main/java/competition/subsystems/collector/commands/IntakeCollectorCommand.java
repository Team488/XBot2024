package competition.subsystems.collector.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class IntakeCollectorCommand extends BaseCommand {
    CollectorSubsystem collector;
    final OperatorInterface oi;
    double intensity = 0.05;

    boolean rumbled = false;

    @Inject
    public IntakeCollectorCommand(CollectorSubsystem collector, OperatorInterface oi) {
        this.collector = collector;
        this.oi = oi;
        addRequirements(collector);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        rumbled = false;
    }

    @Override
    public void execute() {
        collector.intake();
        if (collector.confidentlyHasControlOfNote() && !rumbled) {
            oi.operatorFundamentalsGamepad.getRumbleManager().rumbleGamepad(intensity, 0.3);
            oi.driverGamepad.getRumbleManager().rumbleGamepad(intensity, 0.3);
            rumbled = true;
        }
    }
}