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
    double[] testDistance = {1,2,3,4,5,6,7,8,9,10};
    double[] testRPM = {500,950,1300,1500,1700,2200,2500,2700,3000,3500};

    @Override
    public void setUp() {
        super.setUp();
        converter = new ShooterDistanceToRpmConverter(testDistance,testRPM);
    }

    @Test
    public void testConverter() {
        //testing when distance is exactly equal to a recorded data point (Also testing when the distance is the first and lastelement in the array)
        assertEquals(500, converter.getRPMForDistance(1), 0.00001);
        assertEquals(3500, converter.getRPMForDistance(10), 0.00001);
        assertEquals(2200, converter.getRPMForDistance(6), 0.00001);

        //edge cases
        
    }
}
