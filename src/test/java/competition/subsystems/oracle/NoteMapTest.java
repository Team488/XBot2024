package competition.subsystems.oracle;

import competition.BaseCompetitionTest;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

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
        var knownNotes = noteMap.getAllKnownNotePoses();
        assertEquals(11, knownNotes.length);

        noteMap.addVisionNote(new Pose2d());
        knownNotes = noteMap.getAllKnownNotePoses();
        assertEquals(12, knownNotes.length);

        noteMap.clearVisionNotes();
        knownNotes = noteMap.getAllKnownNotePoses();
        assertEquals(11, knownNotes.length);
    }

    @Test
    public void testAddVisionNote() {
        var knownNotes = noteMap.getAllKnownNotePoses();
        assertEquals(11, knownNotes.length);

        // Only last 10 should be kept, add more than that
        for (int i: IntStream.range(0, 20).toArray()) {
            noteMap.addVisionNote(new Pose2d(i, i, new Rotation2d()));
        }

        // Make sure only the last 10 are kept
        knownNotes = noteMap.getAllKnownNotePoses();
        assertEquals(21, knownNotes.length);

        // Check that the last 10 are the ones we still have
        for (int i: IntStream.range(0, 10).toArray()) {
            assertEquals(new Pose2d(i + 10, i + 10, new Rotation2d()), knownNotes[i + 11].toPose2d());
        }
    }

    @Test
    public void testGetClosestAvailableNote() {
        var notePose = noteMap.getClosestAvailableNote(PoseSubsystem.BlueSpikeMiddle, true);
        assertEquals(PoseSubsystem.BlueSpikeMiddle, notePose.get().toPose2d());

        noteMap.addVisionNote(new Pose2d());
        notePose = noteMap.getClosestAvailableNote(PoseSubsystem.BlueSpikeMiddle, true);
        assertEquals(PoseSubsystem.BlueSpikeMiddle, notePose.get().toPose2d());
    }
}
