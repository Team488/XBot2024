package competition.navigation;

import competition.BaseCompetitionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestGraphField extends BaseCompetitionTest {

    @Test
    public void testSimpleRoute() {
        GraphField graphField = new GraphField();
        var path = graphField.getShortestPath("BlueSubwooferMiddle", "BlueStageCenter");

        assertEquals(4, path.size());
    }

    @Test
    public void avoidMidSpikeThenRestoreIt() {
        GraphField graphField = new GraphField();

        graphField.getNode("BlueSpikeMiddle").setAllWeightsToMax();
        var path = graphField.getShortestPath("BlueSubwooferMiddle", "BlueStageCenter");
        assertEquals(5, path.size());

        graphField.getNode("BlueSpikeMiddle").restoreWeights();
        path = graphField.getShortestPath("BlueSubwooferMiddle", "BlueStageCenter");
        assertEquals(4, path.size());

    }

    @Test
    public void crossField() {
        GraphField graphField = new GraphField();
        var path = graphField.getShortestPath("BlueSubwooferMiddle", "BlueSourceFarthest");

        assertEquals(7, path.size());}
}
