package competition.subsystems.shooter.commands;

import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;
import java.util.Set;

public class SetShooterSpeedFromLocationCommand extends BaseSetpointCommand {
    ShooterWheelSubsystem shooter;
    @Inject
    public SetShooterSpeedFromLocationCommand(ShooterWheelSubsystem shooter){
        super(shooter);
        this.shooter = shooter;

    }

    @Override
    public void initialize() {
        shooter.setTargetValue(shooter.getSpeedForRange());

    }

    @Override
    public void execute() {
        //nothing to do here
    }

}
