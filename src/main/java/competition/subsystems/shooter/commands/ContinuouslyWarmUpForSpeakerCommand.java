package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class ContinuouslyWarmUpForSpeakerCommand extends BaseSetpointCommand {

    ShooterWheelSubsystem shooter;

    @Inject
    public ContinuouslyWarmUpForSpeakerCommand(ShooterWheelSubsystem shooter){
        super(shooter);
        this.shooter = shooter;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        shooter.setTargetValue(shooter.getSpeedForRange());
    }
}