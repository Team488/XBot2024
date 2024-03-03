package competition.navigation;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.trajectory.XbotSwervePoint;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class GraphField {

    private Graph graph;
    private List<Pose2dNode> nodesToAddToGraph;

    public GraphField() {
        graph = new Graph();
        nodesToAddToGraph =new ArrayList<>();
        constructFieldRepresentation();
    }

    private void constructFieldRepresentation() {
        nodesToAddToGraph.clear();

        // Places you may want to go
        // Subwoofer
        var subwooferTop = createBlueAndRedVariants("SubwooferTop", PoseSubsystem.BlueSubwooferTopScoringLocation);
        var subwooferMiddle = createBlueAndRedVariants("SubwooferMiddle", PoseSubsystem.BlueSubwooferMiddleScoringLocation);
        var subwooferBottom = createBlueAndRedVariants("SubwooferBottom", PoseSubsystem.BlueSubwooferBottomScoringLocation);

        // Amp
        var amp = createBlueAndRedVariants("Amp", PoseSubsystem.BlueAmpScoringLocation);

        // Spike Notes
        var spikeTop = createBlueAndRedVariants("SpikeTop", PoseSubsystem.SpikeTop);
        var spikeMiddle = createBlueAndRedVariants("SpikeMiddle", PoseSubsystem.SpikeMiddle);
        var spikeBottom = createBlueAndRedVariants("SpikeBottom", PoseSubsystem.SpikeBottom);

        // Special Shots
        var podiumShot = createBlueAndRedVariants("PodiumShot", PoseSubsystem.BluePodiumScoringLocation);
        var ampFarShot = createBlueAndRedVariants("AmpFarShot", PoseSubsystem.BlueFarAmpScoringLocation);

        // Source
        var sourceNearest = createBlueAndRedVariants("SourceNearest", PoseSubsystem.BlueSourceNearest);
        var sourceMiddle = createBlueAndRedVariants("SourceMiddle", PoseSubsystem.BlueSourceMiddle);
        var sourceFarthest = createBlueAndRedVariants("SourceFarthest", PoseSubsystem.BlueSourceFarthest);

        // Navigation waypoints
        var lowerWhiteLine = createBlueAndRedVariants("LowerWhiteLine", PoseSubsystem.BlueSpikeBottomWhiteLine);
        var upperWhiteLine = createBlueAndRedVariants("UpperWhiteLine", PoseSubsystem.BlueSpikeTopWhiteLine);

        var topWingUpper = createBlueAndRedVariants("TopWingUpper", PoseSubsystem.BlueTopWingUpper);
        var topWingLower = createBlueAndRedVariants("TopWingLower", PoseSubsystem.BlueTopWingLower);
        var bottomWing = createBlueAndRedVariants("BottomWing", PoseSubsystem.BlueBottomWing);

        var stageNW = createBlueAndRedVariants("StageNw", PoseSubsystem.BlueStageNW);
        var stageE = createBlueAndRedVariants("StageE", PoseSubsystem.BlueStageE);
        var stageSW = createBlueAndRedVariants("StageSw", PoseSubsystem.BlueStageSW);
        var stageCenter = createBlueAndRedVariants("StageCenter", PoseSubsystem.BlueStageCenter);
        var southOfStage = createBlueAndRedVariants("SouthOfStage", PoseSubsystem.BlueSouthOfStage);

        var podiumWaypoint = createBlueAndRedVariants("PodiumPrepPoint", PoseSubsystem.podiumWaypoint);

        // Centerline notes - no duplicates!
        Pose2dNode centerLine1 = new Pose2dNode("CenterLine1", PoseSubsystem.CenterLine1);
        Pose2dNode centerLine2 = new Pose2dNode("CenterLine2", PoseSubsystem.CenterLine2);
        Pose2dNode centerLine3 = new Pose2dNode("CenterLine3", PoseSubsystem.CenterLine3);
        Pose2dNode centerLine4 = new Pose2dNode("CenterLine4", PoseSubsystem.CenterLine4);
        Pose2dNode centerLine5 = new Pose2dNode("CenterLine5", PoseSubsystem.CenterLine5);
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
        connectRedAndBlue(spikeMiddle, podiumShot);
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
    }

    private Pair<Pose2dNode, Pose2dNode> createBlueAndRedVariants(String baseName, Pose2d bluePose) {
        Pose2dNode blue = new Pose2dNode("Blue" + baseName, bluePose);
        Pose2dNode red = new Pose2dNode("Red" + baseName, PoseSubsystem.convertBluetoRed(bluePose));
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

    public Pose2dNode getNode(String name) {
        return graph.getNode(name);
    }

    public List<XbotSwervePoint> getShortestPathInSwervePoints(String startName, String endName) {
        List<Pose2dNode> path = getShortestPath(startName, endName);
        Rotation2d finalHeading = path.get(path.size() - 1).getPose().getRotation();
        List<XbotSwervePoint> swervePoints = new ArrayList<>();

        // Force all intermediate points to use the final rotation
        for (Pose2dNode node : path) {
            swervePoints.add(new XbotSwervePoint(new Pose2d(node.getTranslation(), finalHeading), 10));
        }
        return swervePoints;
    }
}
