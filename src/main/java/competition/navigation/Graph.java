package competition.navigation;

import edu.wpi.first.math.geometry.Pose2d;

import java.util.HashMap;
import java.util.Map;

class Graph {
    public Map<String, Pose2dNode> nodes = new HashMap<>();

    public void addNode(Pose2dNode node) {
        nodes.put(node.name, node);
    }

    public void connectNodes(String sourceName, String destinationName) {
        Pose2dNode source = nodes.get(sourceName);
        Pose2dNode destination = nodes.get(destinationName);
        Edge edge = new Edge(source, destination);
        source.addEdge(edge);
        // If the graph is undirected, also connect destination to source
         destination.addEdge(new Edge(destination, source));
    }

    public Pose2dNode getNode(String name) {
        return nodes.get(name);
    }

    public Pose2dNode getClosestNode(Pose2d pose) {
        Pose2dNode closestNode = null;
        double closestDistance = Double.MAX_VALUE;
        for (Pose2dNode node : nodes.values()) {
            double distance = node.getPose().getTranslation().getDistance(pose.getTranslation());
            if (distance < closestDistance) {
                closestNode = node;
                closestDistance = distance;
            }
        }
        return closestNode;
    }
}