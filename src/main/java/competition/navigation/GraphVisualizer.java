package competition.navigation;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
