package competition.subsystems.drive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.math.geometry.Translation2d;
import competition.BaseCompetitionTest;
import competition.subsystems.drive.commands.PointAtNoteCommand;
import xbot.common.math.XYPair;

public class PointAtNoteCommandTest extends BaseCompetitionTest {
    
    @Test
    public void testGetDriveIntent() {
        // completely aligned with the direction we want to go
        // exact y
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(0, 5), new Translation2d(0, 1)), 0.001);
        // exact x
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new Translation2d(1, 0)), 0.001);


        // exact opposite y
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(0, 5), new Translation2d(0, -1)), 0.001);
        // exact opposite x
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new Translation2d(-1, 0)), 0.001);

        // 45 degrees should be half power
        assertEquals(0.5, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new Translation2d(0.5, 0.5)), 0.001);

        // small floating joystick number
        assertTrue(Math.abs(PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new Translation2d(0.01, 0.01))) < 0.02);
    }

}
