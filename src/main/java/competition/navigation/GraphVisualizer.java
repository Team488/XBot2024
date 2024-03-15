package competition.navigation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

class GraphPanel extends JPanel {
    private Graph graph;
    private final int margin = 50; // Margin around the drawing area
    private final int width = 800; // Width of the panel
    private final int height = 800; // Height of the panel
    private final double scaleX;
    private final double scaleY;

    public GraphPanel(Graph graph) {
        setToolTipText(""); // Enable tooltips
        this.graph = graph;
        setPreferredSize(new Dimension(width, height));
        // Assuming the coordinate range is from 0 to 20 for both X and Y
        double rangeX = 20.0;
        double rangeY = 20.0;
        scaleX = (width - 2.0 * margin) / rangeX;
        scaleY = (height - 2.0 * margin) / rangeY;

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (Pose2dNode node : graph.nodes.values()) {
                    int nodeX = (int) (node.getPose().getTranslation().getX() * scaleX) + margin;
                    int nodeY = (int) (height - (node.getPose().getTranslation().getY() * scaleY + margin));
                    if (Math.abs(e.getX() - nodeX) < 10 && Math.abs(e.getY() - nodeY) < 10) {
                        setToolTipText(node.getName());
                        return; // Found a close node, no need to check the rest
                    }
                }
                setToolTipText(null); // No close node, hide tooltip
            }
        });
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        for (Pose2dNode node : graph.nodes.values()) {
            int x = (int) (node.getPose().getTranslation().getX() * scaleX) + margin;
            int y = (int) (height - (node.getPose().getTranslation().getY() * scaleY + margin));
            double distance = Math.sqrt(Math.pow(event.getX() - x, 2) + Math.pow(event.getY() - y, 2));
            if (distance < 10) { // Assuming a node radius of 10 for hit detection
                return node.getName();
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Pose2dNode node : graph.nodes.values()) {
            int x = (int) (node.getPose().getTranslation().getX() * scaleX) + margin;
            int y = (int) (height - (node.getPose().getTranslation().getY() * scaleY + margin)); // Flip Y axis for graphical representation
            g.fillOval(x - 5, y - 5, 10, 10); // Draw nodes as small circles
            for (Edge edge : node.edges) {
                Pose2dNode destination = edge.destination;
                int xDest = (int) (destination.getPose().getTranslation().getX() * scaleX) + margin;
                int yDest = (int) (height - (destination.getPose().getTranslation().getY() * scaleY + margin));
                g.drawLine(x, y, xDest, yDest); // Draw edges as lines
            }
        }
    }
}

public class GraphVisualizer {
    public static void visualize(Graph graph) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new GraphPanel(graph));
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        // Initialize your graph here and call visualize
        var graphField = new GraphField();
        // Add nodes and edges to the graph
        visualize(graphField.getGraph());
    }
}
