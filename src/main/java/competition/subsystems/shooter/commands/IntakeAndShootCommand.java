package competition.subsystems.shooter.commands;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.CollectorSubsystem_Factory;
import competition.subsystems.shooter.ShooterDistanceToRpmConverter;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeAndShootCommand extends BaseCommand {
    CollectorSubsystem collector;
    ShooterDistanceToRpmConverter converter;
    ShooterWheelSubsystem shooter;
    WarmUpShooterCommand warmup;

    @Inject
    public IntakeAndShootCommand(CollectorSubsystem collector, ShooterWheelSubsystem shooter){
        this.collector = collector;
        this.shooter = shooter;
        this.converter = new ShooterDistanceToRpmConverter();
        addRequirements(collector,shooter);

    }

    @Override
    public void initialize() {
        log.info("Initializing");

    }

    @Override
    public void execute() {
        collector.intake();
    }
}
