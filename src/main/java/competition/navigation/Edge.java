package competition.navigation;

class Edge {
    Pose2dNode source;
    Pose2dNode destination;
    double weight;

    public Edge(Pose2dNode source, Pose2dNode destination) {
        this.source = source;
        this.destination = destination;
        calculateProperWeight();
    }

    public void setWeightToMax() {
        this.weight = Double.MAX_VALUE;
    }

    public void calculateProperWeight() {
        this.weight = source.getTranslation().getDistance(destination.getTranslation());
    }
}