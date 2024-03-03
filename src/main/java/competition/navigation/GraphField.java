package competition.navigation;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;

import java.util.List;

public class GraphField {

    public GraphField() {
        Graph graph = new Graph();
    }

    private void addPointsOfInterest() {
        // Subwoofer
        var subwooferTop = createBlueAndRedVariants("SubwooferTop", PoseSubsystem.BlueSubwooferTopScoringLocation);
        var subwooferMiddle = createBlueAndRedVariants("SubwooferMiddle", PoseSubsystem.BlueSubwooferMiddleScoringLocation);
        var subwooferBottom = createBlueAndRedVariants("SubwooferBottom", PoseSubsystem.BlueSubwooferBottomScoringLocation);

        // Spike Notes
        var spikeTop = createBlueAndRedVariants("SpikeTop", PoseSubsystem.SpikeTop);
        var spikeMiddle = createBlueAndRedVariants("SpikeMiddle", PoseSubsystem.SpikeMiddle);
        var spikeBottom = createBlueAndRedVariants("SpikeBottom", PoseSubsystem.SpikeBottom);

        // Specal Shots
    }

    private Pair<Pose2dNode, Pose2dNode> createBlueAndRedVariants(String baseName, Pose2d bluePose) {
        Pose2dNode blue = new Pose2dNode("Blue"+baseName, bluePose);
        Pose2dNode red = new Pose2dNode("Red"+baseName, PoseSubsystem.convertBluetoRed(bluePose));
        return new Pair<>(blue, red);
}
