package competition.subsystems.shooter.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.CollectorSubsystem_Factory;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.shooter.ShooterDistanceToRpmConverter;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeAndShootCommand extends BaseCommand {
    CollectorSubsystem collector;
    ShooterDistanceToRpmConverter converter;
    ShooterWheelSubsystem shooter;
    ArmSubsystem arm;

    @Inject
    public IntakeAndShootCommand(CollectorSubsystem collector, ShooterWheelSubsystem shooter, ArmSubsystem arm){
        this.collector = collector;
        this.shooter = shooter;
        this.converter = new ShooterDistanceToRpmConverter();
        this.arm = arm;
        addRequirements(collector,shooter);

    }

    @Override
    public void initialize() {
        log.info("Initializing");

    }

    @Override
    public void execute() {
        
    }
}
