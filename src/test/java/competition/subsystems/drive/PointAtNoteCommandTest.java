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
    public void testGetDriveIntentBlue() {
        var alliance = Alliance.Blue;
        // completely aligned with the direction we want to go
        // exact y
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(0, 5), new XYPair(-1, 0), alliance), 0.001);
        // exact x
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(0, 1), alliance), 0.001);


        // exact opposite y
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(0, 5), new XYPair(1, 0), alliance), 0.001);
        // exact opposite x
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(0, -1), alliance), 0.001);

        // 45 degrees should be half power
        assertEquals(0.5, PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(0.5, 0.5), alliance), 0.001);

        // small floating joystick number
        assertTrue(Math.abs(PointAtNoteCommand.getDriveIntent(new Translation2d(5, 0), new XYPair(0.01, 0.01), alliance)) < 0.02);
    }

    @Test
    public void testGetDriveIntentRed() {
        var alliance = Alliance.Red;
        // completely aligned with the direction we want to go
        // exact y
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(0, -5), new XYPair(-1, 0), alliance), 0.001);
        // exact x
        assertEquals( 1.0, PointAtNoteCommand.getDriveIntent(new Translation2d(-5, 0), new XYPair(0, 1), alliance), 0.001);


        // exact opposite y
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(0, -5), new XYPair(1, 0), alliance), 0.001);
        // exact opposite x
        assertEquals(-1, PointAtNoteCommand.getDriveIntent(new Translation2d(-5, 0), new XYPair(0, -1), alliance), 0.001);

        // 45 degrees should be half power
        assertEquals(0.5, PointAtNoteCommand.getDriveIntent(new Translation2d(-5, 0), new XYPair(0.5, 0.5), alliance), 0.001);

        // small floating joystick number
        assertTrue(Math.abs(PointAtNoteCommand.getDriveIntent(new Translation2d(-5, 0), new XYPair(0.01, 0.01), alliance)) < 0.02);
    }
}
