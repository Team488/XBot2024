package competition.subsystems.vision;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArraySubscriber;
import org.littletonrobotics.junction.Logger;

public class NoteTracker {
    private final StringArraySubscriber networkTablesSubscriber;

    public NoteTracker(String networkTablesTopic) {
        var table = NetworkTableInstance.getDefault().getTable("SmartDashboard");
        var topic = table.getStringArrayTopic(networkTablesTopic);
        networkTablesSubscriber = topic.subscribe(new String[] {});
    }

    protected NoteTrackerInputsAutoLogged io;

    public String[] getDetections() {
        return io.detectedNotes;
    }

    protected void updateInputs(NoteTrackerInputs inputs) {
        inputs.detectedNotes = networkTablesSubscriber.get();
    }

    public void refreshDataFrame() {
        updateInputs(io);
        // TODO: get a name for the gyro so we don't have to use a hardcoded one.
        Logger.processInputs("NoteTracker", io);
    }
}
