package competition.subsystems;

import competition.BaseCompetitionTest;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NeoTrellisGamepadSubsystemTest extends BaseCompetitionTest {
    @Test
    public void testNeoTrellisGamepadSubsystem() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        assertEquals(32, subsystem.buttonColors.size());
    }

    @Test
    public void testNeoTrellisGamepadSubsystemClearButtonColors() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        subsystem.clearButtonColors();
        for (Color8Bit color : subsystem.buttonColors) {
            assertEquals(new Color8Bit(0, 0, 0), color);
        }
    }

    @Test
    public void testNeoTrellisGamepadSubsystemFillButtonColors() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        subsystem.fillButtonColors(new Color8Bit(1, 2, 3));
        for (Color8Bit color : subsystem.buttonColors) {
            assertEquals(new Color8Bit(1, 2, 3), color);
        }
    }

    @Test
    public void testNeoTrellisGamepadSubsystemSetButtonColor() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        subsystem.setButtonColor(1, new Color8Bit(4, 5, 6));
        assertEquals(new Color8Bit(4, 5, 6), subsystem.buttonColors.get(1));
    }

    @Test
    public void testNeoTrellisGamepadSubsystemGetClearCommand() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        assertNotNull(subsystem.getClearCommand());
    }

    @Test
    public void testNeoTrellisGamepadSubsystemGetFillCommand() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        assertNotNull(subsystem.getFillCommand(new Color8Bit(1, 2, 3)));
    }

    @Test
    public void testNeoTrellisGamepadSubsystemGetSetButtonColorCommand() {
        NeoTrellisGamepadSubsystem subsystem = getInjectorComponent().neoTrellisGamepadSubsystem();
        assertNotNull(subsystem.getSetButtonColorCommand(1, new Color8Bit(4, 5, 6)));
    }
}
