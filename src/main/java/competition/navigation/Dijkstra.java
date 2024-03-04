package competition.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

class Dijkstra {

    public static List<Pose2dNode> findShortestPath(Graph graph, String startName, String endName) {
        Map<Pose2dNode, Double> distances = new HashMap<>();
        Map<Pose2dNode, Pose2dNode> previous = new HashMap<>();
        PriorityQueue<Pose2dNode> nodes = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<Pose2dNode> explored = new HashSet<>();

        for (Pose2dNode node : graph.nodes.values()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            nodes.add(node);
        }

        distances.put(graph.getNode(startName), 0.0);
        nodes.add(graph.getNode(startName));

        while (!nodes.isEmpty()) {
            Pose2dNode current = nodes.poll();
            if (current.name.equals(endName)) {
                return constructPath(previous, current);
            }
            explored.add(current);

            for (Edge edge : current.edges) {
                if (explored.contains(edge.destination)) {
                    continue;
                }
                double newDist = distances.get(current) + edge.weight;
                if (newDist < distances.get(edge.destination)) {
                    nodes.remove(edge.destination); // Remove the old node
                    distances.put(edge.destination, newDist);
                    previous.put(edge.destination, current);
                    nodes.add(edge.destination); // Add the updated node
                }
            }
        }

        return Collections.emptyList(); // Path not found
    }

    private static List<Pose2dNode> constructPath(Map<Pose2dNode, Pose2dNode> previous, Pose2dNode end) {
        List<Pose2dNode> path = new ArrayList<>();
        for (Pose2dNode at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}