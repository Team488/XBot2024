package competition.subsystems.shooter.commands;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseSetpointCommand;
import java.util.function.Supplier;
import javax.inject.Inject;

public class WarmUpShooterCommand extends BaseSetpointCommand {

    ShooterWheelSubsystem shooter;

    private enum Mode {
        CUSTOM,
        DEFAULT
    }
    private Supplier<ShooterWheelSubsystem.TargetRPM> targetRPMSupplier;
    private double customRPM;
    private Mode mode;

    @Inject
    public WarmUpShooterCommand(ShooterWheelSubsystem shooter) {
        super(shooter);
        this.shooter = shooter;
    }

    private void setMode(Mode mode) {
        this.mode = mode;
    }
    public void setTargetRpm(Supplier<ShooterWheelSubsystem.TargetRPM> targetRPMSupplier) {
        setMode(Mode.DEFAULT);
        this.targetRPMSupplier = targetRPMSupplier;
    }

    public void setTargetRpm(ShooterWheelSubsystem.TargetRPM targetRpm) {
        setTargetRpm(()-> targetRpm);
    }

    public void setArbitraryRPM(Double power) {
        setMode(Mode.CUSTOM);
        this.customRPM = power;
    }

    public void initialize() {
        if (mode == Mode.CUSTOM) {
            shooter.setTargetRPM(this.targetRPMSupplier.get());
        }
        else {
            shooter.setTargetValue(this.customRPM);
        }
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