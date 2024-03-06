package competition.navigation;

import competition.BaseCompetitionTest;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestGraph extends BaseCompetitionTest {

    @Test
    public void testGraph() {
        Graph graph = new Graph();
        Pose2dNode node1 = new Pose2dNode("node1", new Pose2d(0, 0, new Rotation2d()));
        Pose2dNode node2 = new Pose2dNode("node2", new Pose2d(1, 1, new Rotation2d()));
        Pose2dNode node3 = new Pose2dNode("node3", new Pose2d(2, 2, new Rotation2d()));
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        graph.connectNodes("node1", "node2");
        graph.connectNodes("node2", "node3");
        assertEquals(node1, graph.getNode("node1"));
        assertEquals(node2, graph.getNode("node2"));
        assertEquals(node3, graph.getNode("node3"));

        var shortPath = Dijkstra.findShortestPath(graph, "node1", "node3");
        assertEquals(3, shortPath.size());
        assertEquals("node1", shortPath.get(0).name);
        assertEquals("node2", shortPath.get(1).name);
        assertEquals("node3", shortPath.get(2).name);
    }

    @Test
    public void testMulitpleRoutes() {
        Graph graph = new Graph();
        Pose2dNode node1 = new Pose2dNode("node1", new Pose2d(0, 0, new Rotation2d()));
        Pose2dNode node2 = new Pose2dNode("node2", new Pose2d(0, 1, new Rotation2d()));
        Pose2dNode node3 = new Pose2dNode("node3", new Pose2d(0, 2, new Rotation2d()));
        Pose2dNode node4 = new Pose2dNode("node4", new Pose2d(2, 2, new Rotation2d()));
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        graph.addNode(node4);
        graph.connectNodes("node1", "node2");
        graph.connectNodes("node2", "node3");
        graph.connectNodes("node1", "node4");
        graph.connectNodes("node3", "node4");

        var shortPath = Dijkstra.findShortestPath(graph, "node1", "node4");
        assertEquals(2, shortPath.size());
        assertEquals("node1", shortPath.get(0).name);
        assertEquals("node4", shortPath.get(1).name);
    }
}
