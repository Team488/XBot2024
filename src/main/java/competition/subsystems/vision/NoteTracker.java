package competition.subsystems.vision;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArraySubscriber;
import org.littletonrobotics.junction.Logger;
import xbot.common.advantage.DataFrameRefreshable;

public class NoteTracker implements DataFrameRefreshable {
    private final StringArraySubscriber networkTablesSubscriber;
    private final String akitName;
    protected NoteTrackerInputsAutoLogged io;

    public NoteTracker(String networkTablesTopic) {
        akitName = networkTablesTopic.split("/")[0];
        var table = NetworkTableInstance.getDefault().getTable("SmartDashboard");
        var topic = table.getStringArrayTopic(networkTablesTopic);
        networkTablesSubscriber = topic.subscribe(new String[] {});
        io = new NoteTrackerInputsAutoLogged();
    }

    public String[] getDetections() {
        return io.detectedNotes;
    }

    protected void updateInputs(NoteTrackerInputs inputs) {
        inputs.detectedNotes = networkTablesSubscriber.get();
    }

    public void refreshDataFrame() {
        updateInputs(io);
        Logger.processInputs(akitName, io);
    }
}
