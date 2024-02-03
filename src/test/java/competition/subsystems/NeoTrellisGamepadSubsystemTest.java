package competition.subsystems;

import competition.BaseCompetitionTest;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NeoTrellisGamepadSubsystemTest extends BaseCompetitionTest {
    @Test
    public void testNeoTrellisGamepadSubsystem() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        assertEquals(32, subsystem.buttonColors.size());

        subsystem.clearButtonColors();
        for (Color8Bit color : subsystem.buttonColors) {
            assertEquals(new Color8Bit(0, 0, 0), color);
        }

        subsystem.fillButtonColors(new Color8Bit(1, 2, 3));
        for (Color8Bit color : subsystem.buttonColors) {
            assertEquals(new Color8Bit(1, 2, 3), color);
        }

        subsystem.setButtonColor(1, new Color8Bit(4, 5, 6));
        assertEquals(new Color8Bit(4, 5, 6), subsystem.buttonColors.get(1));

        subsystem.clearButtonColors();
        for (Color8Bit color : subsystem.buttonColors) {
            assertEquals(new Color8Bit(0, 0, 0), color);
        }
    }
}
