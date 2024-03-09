package competition.navigation;

import edu.wpi.first.math.geometry.Pose2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

    // Method to perform DFS traversal
    public List<Pose2dNode> dfsTraversal() {
        Set<Pose2dNode> visited = new HashSet<>();
        List<Pose2dNode> visitOrder = new ArrayList<>();

        // Assuming the graph is not empty, start DFS from the first node added
        if (!nodes.isEmpty()) {
            Pose2dNode startNode = nodes.values().iterator().next();
            Stack<Pose2dNode> stack = new Stack<>();
            stack.push(startNode);

            while (!stack.isEmpty()) {
                Pose2dNode currentNode = stack.pop();

                if (!visited.contains(currentNode)) {
                    visited.add(currentNode);
                    visitOrder.add(currentNode);

                    // Add all unvisited neighbors to the stack
                    for (Edge edge : currentNode.edges) {
                        Pose2dNode neighbor = edge.getOtherNode(currentNode);
                        if (!visited.contains(neighbor)) {
                            stack.push(neighbor);
                        }
                    }
                }
            }
        }

        return visitOrder;
    }

    public List<Pose2dNode> findEulerianPath() {
        if (!hasEulerianPath()) {
            return null; // No Eulerian path exists
        }

        Stack<Pose2dNode> stack = new Stack<>();
        List<Pose2dNode> path = new ArrayList<>();
        Pose2dNode current = findStartNodeForEulerianPath();

        stack.push(current);

        while (!stack.isEmpty()) {
            if (current.hasUnusedEdges()) {
                stack.push(current);
                Edge edge = current.getUnusedEdge(); // Implement this method in Pose2dNode to get an unused edge
                edge.use(); // Mark this edge as used; implement this method in Edge
                current = edge.getOtherNode(current); // Implement this method in Edge to get the node at the other end
            } else {
                path.add(current);
                current = stack.pop();
            }
        }

        return path;
    }

    private boolean hasEulerianPath() {
        int oddDegreeVertices = 0;
        for (Pose2dNode node : nodes.values()) {
            if (node.getDegree() % 2 != 0) {
                oddDegreeVertices++;
            }
        }
        return oddDegreeVertices == 0 || oddDegreeVertices == 2;
    }

    private Pose2dNode findStartNodeForEulerianPath() {
        Pose2dNode start = null;
        for (Pose2dNode node : nodes.values()) {
            if (node.getDegree() % 2 != 0) {
                return node; // Return the first odd degree vertex as start node
            }
            if (start == null) {
                start = node; // Choose the first node as start if no odd degree vertices
            }
        }
        return start;
    }
}