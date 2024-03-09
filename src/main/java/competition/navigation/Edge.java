package competition.navigation;

class Edge {
    Pose2dNode source;
    Pose2dNode destination;
    double weight;
    boolean isUsed = false;

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

    // Method to mark this edge as used
    public void use() {
        this.isUsed = true;
    }

    // Method to check if this edge is used
    public boolean isUsed() {
        return isUsed;
    }

    // Method to get the node at the other end of the edge
    public Pose2dNode getOtherNode(Pose2dNode node) {
        if (node.equals(source)) {
            return destination;
        } else if (node.equals(destination)) {
            return source;
        } else {
            return null; // This case should never happen if the edge connects the given node
        }
    }
}