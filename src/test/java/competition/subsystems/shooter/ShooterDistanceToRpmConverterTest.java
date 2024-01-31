package competition.subsystems.shooter;

import competition.BaseCompetitionTest;
import competition.subsystems.pose.PoseSubsystem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ShooterDistanceToRpmConverterTest extends BaseCompetitionTest {
    ShooterDistanceToRpmConverter converter;
    PoseSubsystem pose;
    //meters
    double[] testDistance = {1,2};
    double[] testRPM = {500,950};

    @Override
    public void setUp() {
        super.setUp();
        converter = new ShooterDistanceToRpmConverter(testDistance,testRPM);
    }

    public void compareConverter(){
        assertEquals(500,converter.getSecantLineSlope(pose) * pose.getDistanceFromSpeaker(),0.00001);
    }
    @Test
    public void testConverter(){
        compareConverter();
    }
}
