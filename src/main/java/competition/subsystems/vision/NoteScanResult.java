package competition.subsystems.vision;

public class NoteScanResult {
    private NoteDetectionSource source;
    private SimpleNote note;

    public NoteScanResult(NoteDetectionSource source, SimpleNote note) {
        this.source = source;
        this.note = note;
    }

    public NoteDetectionSource getSource() {
        return source;
    }

    public SimpleNote getNote() {
        return note;
    }
}
