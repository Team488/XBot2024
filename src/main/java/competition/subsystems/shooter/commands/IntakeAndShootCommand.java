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

        //collects the game piece and gets the arm into position
        //if arm is able to extend while intaking then I can change this
        while(!collector.getGamePieceInControl()){
            collector.intake();
        }
        while (!arm.isMaintainerAtGoal()) {
            arm.extend();
        }
    }

    @Override
    public void execute() {
        //sets the shooter to desired RPM then fires if note is collected and ready
        shooter.setTargetValue(shooter.getSpeedForRange());
        if(shooter.isMaintainerAtGoal() && collector.getGamePieceReady()){
            collector.fire();
        }
    }

    @Override
    public boolean isFinished() {
        //if note is no longer in posession (aka its been fired), the command is finished
        return !collector.getGamePieceInControl();
    }
}
