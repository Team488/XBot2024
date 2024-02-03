package competition.subsystems.shooter;

import competition.BaseCompetitionTest;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class WarmUpShooterCommandTest extends BaseCompetitionTest {

    WarmUpShooterCommand warmUpShooterCommand;
    ShooterWheelSubsystem shooter;


    @Override
    public void setUp() {
        super.setUp();
        shooter = getInjectorComponent().shooterSubsystem();
        warmUpShooterCommand = new WarmUpShooterCommand(shooter);
    }

    @Test
    public void testSetTargetRPM() {
        Supplier<ShooterWheelSubsystem.TargetRPM> safeRPMSupplier = ()->ShooterWheelSubsystem.TargetRPM.SAFE;
        warmUpShooterCommand.setTargetRpm(safeRPMSupplier);
        warmUpShooterCommand.initialize();
        warmUpShooterCommand.execute();

        double safeRPM = 500;
        assertEquals(Optional.ofNullable(shooter.getTargetValue()), Optional.of(safeRPM));

    }
}