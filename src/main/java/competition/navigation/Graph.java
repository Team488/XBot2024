package competition.navigation;

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
}