package competition.subsystems.shooter.commands;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseSetpointCommand;
import java.util.function.Supplier;
import javax.inject.Inject;

public class WarmUpShooterCommand extends BaseSetpointCommand {

    ShooterWheelSubsystem shooter;
    private Supplier<ShooterWheelSubsystem.TargetRPM> targetRPMSupplier;

    @Inject
    public WarmUpShooterCommand(ShooterWheelSubsystem shooter) {
        super(shooter);
        this.shooter = shooter;
    }

    public void setTargetRpm(Supplier<ShooterWheelSubsystem.TargetRPM> targetRPMSupplier) {
        this.targetRPMSupplier = targetRPMSupplier;
    }

    public void setTargetRpm(ShooterWheelSubsystem.TargetRPM targetRpm) {
        setTargetRpm(()-> targetRpm);
    }

    public void initialize() {
        shooter.setTargetRPM(this.targetRPMSupplier.get());
        log.info("Initialized: " + targetRPMSupplier.get());

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