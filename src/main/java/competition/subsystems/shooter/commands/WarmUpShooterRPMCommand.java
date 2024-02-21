package competition.subsystems.shooter.commands;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import xbot.common.command.BaseSetpointCommand;
import java.util.function.Supplier;
import javax.inject.Inject;

public class WarmUpShooterRPMCommand extends BaseSetpointCommand {

    ShooterWheelSubsystem shooter;
    private Supplier<ShooterWheelTargetSpeeds> customRPM;

    @Inject
    public WarmUpShooterRPMCommand(ShooterWheelSubsystem shooter) {
        super(shooter);
        this.shooter = shooter;
    }

    public void setTargetRpm(Supplier<ShooterWheelTargetSpeeds> customRPM) {
        this.customRPM = customRPM;
    }

    public void setTargetRpm(ShooterWheelTargetSpeeds customRPMs) {
        setTargetRpm(()-> customRPMs);
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