package competition.navigation;

import competition.BaseCompetitionTest;
import competition.subsystems.pose.PointOfInterest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestGraphField extends BaseCompetitionTest {

    @Test
    public void testSimpleRoute() {
        GraphField graphField = new GraphField();
        var path = graphField.getShortestPath(PointOfInterest.SubwooferMiddleScoringLocation, PointOfInterest.StageCenter);

        assertEquals(4, path.size());
    }

    @Test
    public void avoidMidSpikeThenRestoreIt() {
        GraphField graphField = new GraphField();

        graphField.getNode(PointOfInterest.SpikeMiddle).setAllWeightsToMax();
        var path = graphField.getShortestPath(PointOfInterest.SubwooferMiddleScoringLocation, PointOfInterest.StageCenter);
        assertEquals(5, path.size());

        graphField.getNode(PointOfInterest.SpikeMiddle).restoreWeights();
        path = graphField.getShortestPath(PointOfInterest.SubwooferMiddleScoringLocation, PointOfInterest.StageCenter);
        assertEquals(4, path.size());

    }

    @Test
    public void crossField() {
        GraphField graphField = new GraphField();
        var path = graphField.getShortestPath(PointOfInterest.SubwooferMiddleScoringLocation, PointOfInterest.SourceFarthest);

        assertEquals(7, path.size());}
}
