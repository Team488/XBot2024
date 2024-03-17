package competition.subsystems.lights;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import competition.BaseCompetitionTest;

public class LightSubsystemTest extends BaseCompetitionTest {
    public static void assertArraysEqual(boolean[] expected, boolean[] actual) {
        assertEquals("number of bits should match", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Expect bit " + i + " to match", expected[i], actual[i]);
        }
    }   

    @Test
    public void testStateToBitsMapping() {
        assertArraysEqual(new boolean[] {false, false, false, false}, LightSubsystem.convertIntToBits(0));
        assertArraysEqual(new boolean[] {true, false, false, false}, LightSubsystem.convertIntToBits(1));
        assertArraysEqual(new boolean[] {true, true, true, true}, LightSubsystem.convertIntToBits(15));
    }
}
