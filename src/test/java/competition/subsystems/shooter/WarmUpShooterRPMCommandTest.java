package competition.subsystems.shooter;

import competition.BaseCompetitionTest;
import competition.subsystems.shooter.commands.WarmUpShooterRPMCommand;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class WarmUpShooterRPMCommandTest extends BaseCompetitionTest {

    WarmUpShooterRPMCommand warmUpShooterRPMCommand;
    ShooterWheelSubsystem shooter;


    @Override
    public void setUp() {
        super.setUp();
        shooter = getInjectorComponent().shooterSubsystem();
        warmUpShooterRPMCommand = new WarmUpShooterRPMCommand(shooter);
    }

    @Test
    public void testSetTargetRPM() {
        Supplier<Double> randomRPM = ()->(Double)100.0;
        warmUpShooterRPMCommand.setTargetRpm(randomRPM);
        warmUpShooterRPMCommand.initialize();
        warmUpShooterRPMCommand.execute();

        assertEquals(Optional.ofNullable(shooter.getTargetValue()), Optional.of(100.0));

    }
}