package competition.navigation;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

import java.util.ArrayList;
import java.util.List;

public class Pose2dNode {
    String name;
    Pose2d pose;
    List<Edge> edges = new ArrayList<>();

    public Pose2dNode(String name, Pose2d pose) {
        this.name = name;
        this.pose = pose;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public Translation2d getTranslation() {
        return pose.getTranslation();
    }
}