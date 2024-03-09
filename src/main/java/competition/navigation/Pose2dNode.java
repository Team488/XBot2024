package competition.navigation;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;

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

    public Pose2d getPose() {
        return pose;
    }

    public void setAllWeightsToMax() {
        for (Edge edge : edges) {
            edge.setWeightToMax();
        }
    }

    public void restoreWeights() {
        for (Edge edge : edges) {
            edge.calculateProperWeight();
        }
    }

    public Pair<Pose2dNode,Double> getLinkedNode(String name) {
        for (Edge edge : edges) {
            if (edge.destination.name.equals(name)) {
                return new Pair<>(edge.destination, edge.weight);
            }
        }
        return null;
    }

    public Trajectory visualizeConnectionsAsTrajectory() {
        // first, start at your own current location
        var initial = new edu.wpi.first.math.trajectory.Trajectory.State();
        initial.poseMeters = pose;

        var wpiStates = new ArrayList<edu.wpi.first.math.trajectory.Trajectory.State>();
        wpiStates.add(initial);
        // visit each edge and add a trajectory point for the source and destination
        for (Edge edge : edges) {
            var outgoing = new edu.wpi.first.math.trajectory.Trajectory.State();
            outgoing.poseMeters = edge.source.getPose();
            wpiStates.add(outgoing);
            var incoming = new edu.wpi.first.math.trajectory.Trajectory.State();
            incoming.poseMeters = edge.destination.getPose();
            wpiStates.add(incoming);
        }
        return new Trajectory(wpiStates);
    }

    // Method to check if there are any unused edges
    public boolean hasUnusedEdges() {
        return edges.stream().anyMatch(edge -> !edge.isUsed());
    }

    // Method to get an unused edge
    public Edge getUnusedEdge() {
        return edges.stream().filter(edge -> !edge.isUsed()).findFirst().orElse(null);
    }

    // Method to get the degree of the node
    public int getDegree() {
        return edges.size();
    }
}