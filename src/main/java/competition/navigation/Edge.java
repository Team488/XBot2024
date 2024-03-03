package competition.navigation;

import edu.wpi.first.math.geometry.Pose2d;

class Edge {
    Pose2dNode source;
    Pose2dNode destination;
    double weight;

    public Edge(Pose2dNode source, Pose2dNode destination) {
        this.source = source;
        this.destination = destination;
        this.weight = source.getTranslation().getDistance(destination.getTranslation());
    }
}