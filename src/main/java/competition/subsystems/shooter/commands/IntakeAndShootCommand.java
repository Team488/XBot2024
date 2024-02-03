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
    ShooterWheelSubsystem shooter;
    ArmSubsystem arm;

    @Inject
    public IntakeAndShootCommand(CollectorSubsystem collector, ShooterWheelSubsystem shooter, ArmSubsystem arm){
        addRequirements(collector,shooter,arm);
        this.collector = collector;
        this.shooter = shooter;
        this.arm = arm;
    }

    @Override
    public void initialize() {
        log.info("Initializing");

        //collects the game piece and gets it in
        while(!collector.getGamePieceCollected()){
            collector.intake();
        }
        while (!arm.isMaintainerAtGoal()) {
            arm.extend();
        }
    }

    @Override
    public void execute() {
        shooter.setTargetValue(shooter.getSpeedForRange());
        if(shooter.isMaintainerAtGoal()){

        }
    }

    @Override
    public boolean isFinished() {
        return !collector.getGamePieceCollected();
    }
}
