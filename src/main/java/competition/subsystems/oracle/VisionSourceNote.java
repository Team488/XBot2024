package competition.subsystems.oracle;

public class VisionSourceNote {
    private Note note;

    private double timestamp;

    public VisionSourceNote(Note note, double timestamp) {
        this.note = note;
        this.timestamp = timestamp;
    }

    public Note getNote() {
        return note;
    }

    public double getTimestamp() {
        return timestamp;
    }
}
