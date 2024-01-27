package competition.subsystems.shooter.commands;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseSetpointCommand;
import java.util.function.Supplier;
import javax.inject.Inject;

public class WarmUpShooterRPMCommand extends BaseSetpointCommand {

    ShooterWheelSubsystem shooter;
    private Supplier<Double> customRPM;

    @Inject
    public WarmUpShooterRPMCommand(ShooterWheelSubsystem shooter) {
        super(shooter);
        this.shooter = shooter;
    }

    public void setTargetRpm(Supplier<Double> customRPM) {
        this.customRPM = customRPM;
    }

    public void setTargetRpm(Double customRPM) {
        setTargetRpm(()-> customRPM);
    }

    public void initialize() {
        shooter.setTargetValue(this.customRPM.get());
    }

    @Override
    public void execute() {
        // No-op. Wait for the arms to get to the target.
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}