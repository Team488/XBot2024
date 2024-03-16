package competition.navigation;

import competition.subsystems.pose.PointOfInterest;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import xbot.common.trajectory.ProvidesWaypoints;
import xbot.common.trajectory.XbotSwervePoint;

import java.util.ArrayList;
import java.util.List;

public class GraphField implements ProvidesWaypoints {

    private Graph graph;
    private List<Pose2dNode> nodesToAddToGraph;

    public GraphField() {
        graph = new Graph();
        nodesToAddToGraph =new ArrayList<>();
        constructFieldRepresentation();
    }

    //CHECKSTYLE:OFF

    private void constructFieldRepresentation() {
        nodesToAddToGraph.clear();

        // Places you may want to go
        // Subwoofer
        var subwooferTop = createBlueAndRedVariants(PointOfInterest.SubwooferTopScoringLocation);
        var subwooferMiddle = createBlueAndRedVariants(PointOfInterest.SubwooferMiddleScoringLocation);
        var subwooferBottom = createBlueAndRedVariants(PointOfInterest.SubwooferBottomScoringLocation);

        // Amp
        var amp = createBlueAndRedVariants(PointOfInterest.AmpScoringLocation);

        // Spike Notes
        var spikeTop = createBlueAndRedVariants(PointOfInterest.SpikeTop);
        var spikeMiddle = createBlueAndRedVariants(PointOfInterest.SpikeMiddle);
        var spikeBottom = createBlueAndRedVariants(PointOfInterest.SpikeBottom);

        // Special Shots
        var podiumShot = createBlueAndRedVariants(PointOfInterest.PodiumScoringLocation);
        var ampFarShot = createBlueAndRedVariants(PointOfInterest.AmpFarScoringLocation);
        var bottomSpikeCloserToSpeaker = createBlueAndRedVariants(PointOfInterest.BottomSpikeCloserToSpeakerScoringLocation);
        var topSpikeCloserToSpeaker = createBlueAndRedVariants(PointOfInterest.TopSpikeCloserToSpeakerScoringLocation);
        var oneRobotAwayFromCenterSubwoofer = createBlueAndRedVariants(PointOfInterest.OneRobotAwayFromCenterSubwooferScoringLocation);
        var shotFromMiddleSpike = createBlueAndRedVariants(PointOfInterest.MiddleSpikeScoringLocation);

        // Source
        var sourceNearest = createBlueAndRedVariants(PointOfInterest.SourceNearest);
        var sourceMiddle = createBlueAndRedVariants(PointOfInterest.SourceMiddle);
        var sourceFarthest = createBlueAndRedVariants(PointOfInterest.SourceFarthest);

        // Navigation waypoints
        var lowerWhiteLine = createBlueAndRedVariants(PointOfInterest.SpikeBottomWhiteLine);
        var upperWhiteLine = createBlueAndRedVariants(PointOfInterest.SpikeTopWhiteLine);

        var topWingUpper = createBlueAndRedVariants(PointOfInterest.TopWingUpper);
        var topWingLower = createBlueAndRedVariants(PointOfInterest.TopWingLower);
        var bottomWing = createBlueAndRedVariants(PointOfInterest.BottomWing);

        var stageNW = createBlueAndRedVariants(PointOfInterest.StageNW);
        var stageE = createBlueAndRedVariants(PointOfInterest.StageE);
        var stageSW = createBlueAndRedVariants(PointOfInterest.StageSW);
        var stageCenter = createBlueAndRedVariants(PointOfInterest.StageCenter);
        var southOfStage = createBlueAndRedVariants(PointOfInterest.SouthOfStage);

        var podiumWaypoint = createBlueAndRedVariants(PointOfInterest.PodiumWaypoint);

        // Centerline notes - no duplicates!
        Pose2dNode centerLine1 = new Pose2dNode(PointOfInterest.CenterLine1.toString(), PointOfInterest.CenterLine1.getLocation());
        Pose2dNode centerLine2 = new Pose2dNode(PointOfInterest.CenterLine2.toString(), PointOfInterest.CenterLine2.getLocation());
        Pose2dNode centerLine3 = new Pose2dNode(PointOfInterest.CenterLine3.toString(), PointOfInterest.CenterLine3.getLocation());
        Pose2dNode centerLine4 = new Pose2dNode(PointOfInterest.CenterLine4.toString(), PointOfInterest.CenterLine4.getLocation());
        Pose2dNode centerLine5 = new Pose2dNode(PointOfInterest.CenterLine5.toString(), PointOfInterest.CenterLine5.getLocation());
        nodesToAddToGraph.add(centerLine1);
        nodesToAddToGraph.add(centerLine2);
        nodesToAddToGraph.add(centerLine3);
        nodesToAddToGraph.add(centerLine4);
        nodesToAddToGraph.add(centerLine5);

        // All nodes created. Get them in the graph.
        for (Pose2dNode node : nodesToAddToGraph) {
            graph.addNode(node);
        }

        // Now for edges. As a first pass, connect each point of interest to its neighbors.
        connectRedAndBlue(subwooferTop, upperWhiteLine);

        connectRedAndBlue(subwooferBottom, lowerWhiteLine);

        connectRedAndBlue(subwooferMiddle, upperWhiteLine);
        connectRedAndBlue(subwooferMiddle, lowerWhiteLine);
        connectRedAndBlue(subwooferMiddle, spikeTop);
        connectRedAndBlue(subwooferMiddle, spikeMiddle);

        connectRedAndBlue(amp, upperWhiteLine);
        connectRedAndBlue(amp, ampFarShot);
        connectRedAndBlue(amp, spikeTop);

        connectRedAndBlue(ampFarShot, spikeTop);
        connectRedAndBlue(ampFarShot, topWingUpper);

        connectRedAndBlue(spikeTop, upperWhiteLine);
        connectRedAndBlue(spikeTop, topWingUpper);
        connectRedAndBlue(spikeTop, topWingLower);
        connectRedAndBlue(spikeTop, spikeMiddle);

        connectRedAndBlue(spikeMiddle, topWingLower);
        connectRedAndBlue(spikeMiddle, stageNW);
        //connectRedAndBlue(spikeMiddle, podiumShot); // Try forcing the podium shot only reasonably available from the bottom spike.
        connectRedAndBlue(spikeMiddle, podiumWaypoint);

        connectRedAndBlue(podiumShot, podiumWaypoint);

        connectRedAndBlue(spikeBottom, podiumWaypoint);
        connectRedAndBlue(spikeBottom, lowerWhiteLine);

        connectRedAndBlue(sourceNearest, sourceMiddle);
        connectRedAndBlue(sourceMiddle, sourceFarthest);

        // Now, connect any remaining leftover navigation waypoints.

        connectRedAndBlue(lowerWhiteLine, podiumWaypoint);
        connectRedAndBlue(lowerWhiteLine, southOfStage);

        connectRedAndBlue(stageNW, stageE);
        connectRedAndBlue(stageNW, stageCenter);
        connectRedAndBlue(stageNW, stageSW);

        connectRedAndBlue(stageCenter, stageE);
        connectRedAndBlue(stageCenter, stageSW);

        connectRedAndBlue(stageSW, stageE);
        connectRedAndBlue(stageSW, southOfStage);

        connectRedAndBlue(southOfStage, bottomWing);

        // special case - connect the wing nodes together across the centerline.
        graph.connectNodes(bottomWing.getFirst().name, bottomWing.getSecond().name);
        graph.connectNodes(topWingLower.getFirst().name, topWingLower.getSecond().name);
        graph.connectNodes(topWingUpper.getFirst().name, topWingUpper.getSecond().name);

        // special case - centerline notes connect to each other
        graph.connectNodes(centerLine1.name, centerLine2.name);
        graph.connectNodes(centerLine2.name, centerLine3.name);
        graph.connectNodes(centerLine3.name, centerLine4.name);
        graph.connectNodes(centerLine4.name, centerLine5.name);

        // special case - centerline notes connect to wing notes of both colors
        graph.connectNodes(centerLine1.name, topWingUpper.getFirst().name);
        graph.connectNodes(centerLine1.name, topWingUpper.getSecond().name);
        graph.connectNodes(centerLine1.name, topWingLower.getFirst().name);
        graph.connectNodes(centerLine1.name, topWingLower.getSecond().name);

        graph.connectNodes(centerLine2.name, topWingUpper.getFirst().name);
        graph.connectNodes(centerLine2.name, topWingUpper.getSecond().name);
        graph.connectNodes(centerLine2.name, topWingLower.getFirst().name);
        graph.connectNodes(centerLine2.name, topWingLower.getSecond().name);
        graph.connectNodes(centerLine2.name, stageE.getFirst().name);
        graph.connectNodes(centerLine2.name, stageE.getSecond().name);

        graph.connectNodes(centerLine3.name, stageE.getFirst().name);
        graph.connectNodes(centerLine3.name, stageE.getSecond().name);

        graph.connectNodes(centerLine4.name, stageE.getFirst().name);
        graph.connectNodes(centerLine4.name, stageE.getSecond().name);
        graph.connectNodes(centerLine4.name, bottomWing.getFirst().name);
        graph.connectNodes(centerLine4.name, bottomWing.getSecond().name);

        graph.connectNodes(centerLine5.name, bottomWing.getFirst().name);
        graph.connectNodes(centerLine5.name, bottomWing.getSecond().name);

        // special case - connect the lower wing to the opposite color source
        connectRedToBlue(bottomWing, sourceNearest);
        connectRedToBlue(bottomWing, sourceMiddle);
        connectRedToBlue(bottomWing, sourceFarthest);

        // extra connections for speed:
        connectRedAndBlue(subwooferBottom, southOfStage);

        // New scoring locations added
        connectRedAndBlue(topSpikeCloserToSpeaker, spikeTop);
        connectRedAndBlue(bottomSpikeCloserToSpeaker, podiumWaypoint);
        connectRedAndBlue(oneRobotAwayFromCenterSubwoofer, spikeMiddle);
        connectRedAndBlue(shotFromMiddleSpike, spikeMiddle);
    }

    //CHECKSTYLE:ON

    private Pair<Pose2dNode, Pose2dNode> createBlueAndRedVariants(PointOfInterest poi) {
        Pose2dNode blue = new Pose2dNode(poi.getBlueName(), poi.getBlueLocation());
        Pose2dNode red = new Pose2dNode(poi.getRedName(), poi.getRedLocation());
        nodesToAddToGraph.add(blue);
        nodesToAddToGraph.add(red);
        return new Pair<>(blue, red);
    }

    /**
     * Will connect red to red and blue to blue.
     * @param origin first Node
     * @param destination second Node
     */
    private void connectRedAndBlue(Pair<Pose2dNode, Pose2dNode> origin, Pair<Pose2dNode, Pose2dNode> destination) {
        graph.connectNodes(origin.getFirst().name, destination.getFirst().name);
        graph.connectNodes(origin.getSecond().name, destination.getSecond().name);
    }

    private void connectRedToBlue(Pair<Pose2dNode, Pose2dNode> origin, Pair<Pose2dNode, Pose2dNode> destination) {
        graph.connectNodes(origin.getFirst().name, destination.getSecond().name);
        graph.connectNodes(origin.getSecond().name, destination.getFirst().name);
    }

    public List<Pose2dNode> getShortestPath(String startName, String endName) {
        return Dijkstra.findShortestPath(graph, startName, endName);
    }

    public List<Pose2dNode> getShortestPath(PointOfInterest start, PointOfInterest end) {
        return getShortestPath(start.getName(), end.getName());
    }

    public Pose2dNode getNode(String name) {
        return graph.getNode(name);
    }

    public Pose2dNode getNode(PointOfInterest poi) {
        return getNode(poi.getName());
    }

    public List<XbotSwervePoint> getShortestPathInSwervePoints(String startName, String endName) {
        List<Pose2dNode> path = getShortestPath(startName, endName);
        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        for (Pose2dNode node : path) {
            swervePoints.add(new XbotSwervePoint(node.pose, 10));
        }

        return swervePoints;
    }

    @Override
    public List<XbotSwervePoint> generatePath(Pose2d start, Pose2d end) {
        // Iterate through all nodes and find the one closest to the start, and the one closest to the end.
        var closestToStart = graph.getClosestNode(start);
        var closestToEnd = graph.getClosestNode(end);

        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        //swervePoints.add(new XbotSwervePoint(start, 10));
        swervePoints.addAll(getShortestPathInSwervePoints(closestToStart.name, closestToEnd.name));
        swervePoints.add(new XbotSwervePoint(end, 10));

        // Force all intermediate points to use the final rotation
        for (XbotSwervePoint point : swervePoints) {
            point.keyPose = new Pose2d(point.keyPose.getTranslation(), end.getRotation());
        }

        return swervePoints;
    }

    public List<Pair<String,Trajectory>> visualizeNodesAndEdges() {
        List<Pair<String,Trajectory>> trajectories = new ArrayList<>();
        for (Pose2dNode node : graph.nodes.values()) {
            trajectories.add(new Pair<>(node.name+"Node", generateTrajectoryForNode(node)));
            trajectories.add(new Pair<>(node.name+"Edges", node.visualizeConnectionsAsTrajectory()));
        }
        return trajectories;
    }

    private Trajectory generateTrajectoryForNode(Pose2dNode node) {

        var wpiStates = new ArrayList<edu.wpi.first.math.trajectory.Trajectory.State>();
        edu.wpi.first.math.trajectory.Trajectory.State topRight = new edu.wpi.first.math.trajectory.Trajectory.State();
        topRight.poseMeters = new Pose2d(node.getTranslation().plus(new Translation2d(0.1, .1)), new Rotation2d());
        edu.wpi.first.math.trajectory.Trajectory.State topLeft = new edu.wpi.first.math.trajectory.Trajectory.State();
        topLeft.poseMeters = new Pose2d(node.getTranslation().plus(new Translation2d(-0.1, .1)), new Rotation2d());
        edu.wpi.first.math.trajectory.Trajectory.State bottomRight = new edu.wpi.first.math.trajectory.Trajectory.State();
        bottomRight.poseMeters = new Pose2d(node.getTranslation().plus(new Translation2d(0.1, -0.1)), new Rotation2d());
        edu.wpi.first.math.trajectory.Trajectory.State bottomLeft = new edu.wpi.first.math.trajectory.Trajectory.State();
        bottomLeft.poseMeters = new Pose2d(node.getTranslation().plus(new Translation2d(-0.1, -0.1)), new Rotation2d());


        wpiStates.add(topLeft);
        wpiStates.add(topRight);
        wpiStates.add(bottomLeft);
        wpiStates.add(bottomRight);
        return new edu.wpi.first.math.trajectory.Trajectory(wpiStates);
    }

    public List<Pose2dNode> getListOfConnectedNodes() {
        return graph.dfsTraversal();
    }

    public Graph getGraph() {
        return graph;
    }
}
