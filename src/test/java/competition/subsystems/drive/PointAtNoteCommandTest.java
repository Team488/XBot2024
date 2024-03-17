package competition.subsystems.drive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import competition.BaseCompetitionTest;
import competition.subsystems.drive.commands.PointAtNoteCommand;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.math.XYPair;

public class PointAtNoteCommandTest extends BaseCompetitionTest {
    
    @Test
    public void testGetDriveIntent() {
        // completely aligned with the direction we want to go
        // exact y
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(0, 5), new XYPair(0, -1)), 0.001);
        // exact x
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(1, 0)), 0.001);


        // exact opposite y
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(0, 5), new XYPair(0, 1)), 0.001);
        // exact opposite x
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(-1, 0)), 0.001);

        // 45 degrees should be half power
        assertEquals(1 / Math.sqrt(2), PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(1, 1)), 0.001);
    }
}
