package competition.subsystems.oracle;

import competition.BaseCompetitionTest;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoteMapTest extends BaseCompetitionTest {
    private NoteMap noteMap;

    @Before
    public void setUp() {
        super.setUp();
        this.noteMap = new NoteMap();
    }

    @Test
    public void testGetAllKnownNotes() {
        var knownNotes = noteMap.getAllKnownNotes();
        assertEquals(11, knownNotes.length);

        noteMap.addVisionNote(new Pose2d());
        knownNotes = noteMap.getAllKnownNotes();
        assertEquals(12, knownNotes.length);

        noteMap.clearVisionNotes();
        knownNotes = noteMap.getAllKnownNotes();
        assertEquals(11, knownNotes.length);
    }

    @Test
    public void testGetClosestAvailableNote() {
        var notePose = noteMap.getClosestAvailableNote(PoseSubsystem.BlueSpikeMiddle);
        assertEquals(PoseSubsystem.BlueSpikeMiddle, notePose.toPose2d());
    }
}
