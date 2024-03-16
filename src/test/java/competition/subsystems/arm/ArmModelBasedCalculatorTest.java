package competition.subsystems.arm;

import com.fasterxml.jackson.databind.Module;
import competition.BaseCompetitionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArmModelBasedCalculatorTest extends BaseCompetitionTest {

    ArmModelBasedCalculator calculator;

    @Override
    public void setUp() {
        super.setUp();
        calculator = getInjectorComponent().armModelBasedCalculator();
    }

    @Test
    public void testGetSLArmAngleFromDistance() {
        // Test the function that calculates the straight line (SL) angle from the robot to the speaker
        // (Will likely fail if the equation is changed)
        assertEquals(54.7, calculator.getArmSLAngleFromDistance(1.0), .5);
        assertEquals(54.7, calculator.getArmSLAngleFromDistance(1.15), .5);
        assertEquals(44.76, calculator.getArmSLAngleFromDistance(1.6), .5);
        assertEquals(37.17, calculator.getArmSLAngleFromDistance(2.032), .5);
        assertEquals(27.3, calculator.getArmSLAngleFromDistance(2.8194), .5);
        assertEquals(12.23, calculator.getArmSLAngleFromDistance(6.09), .5);
    }

    @Test
    public void testGetExtensionForSpeakerDistance() {
        // Test the function that calculates the empirical linear actuator extension (mm) given the distance from the robot to the speaker (m)
        // (Will likely fail if the equation is changed)
        double delta_error = 1.0; // acceptable arm extension error in mm

        assertEquals(44.847, calculator.getExtensionForSpeakerDistance(2.338), delta_error);
        assertEquals(57.082, calculator.getExtensionForSpeakerDistance(2.83), delta_error);
        assertEquals(71.804, calculator.getExtensionForSpeakerDistance(3.56), delta_error);
        assertEquals(79.0, calculator.getExtensionForSpeakerDistance(4.0), delta_error);
        assertEquals(0.0, calculator.getExtensionForSpeakerDistance(1.146302), delta_error);
        assertEquals(0.0, calculator.getExtensionForSpeakerDistance(1.0), delta_error);
        assertEquals(84.24, calculator.getExtensionForSpeakerDistance(5.0), delta_error);
        assertEquals(84.24, calculator.getExtensionForSpeakerDistance(10.0), delta_error);
    }
    @Test
    public void testGetEmpiricalArmExtensionFromDistance() {
        // Test the function that calculates the empirical linear actuator extension (mm) given the distance from the robot to the speaker (m)
        // (Will likely fail if the equation is changed)
        double delta_error = 1.0; // acceptable arm extension error in mm

        assertEquals(44.847, calculator.getEmpiricalArmExtensionFromDistance(2.338), delta_error);
        assertEquals(57.082, calculator.getEmpiricalArmExtensionFromDistance(2.83), delta_error);
        assertEquals(71.804, calculator.getEmpiricalArmExtensionFromDistance(3.56), delta_error);
        assertEquals(79.0, calculator.getEmpiricalArmExtensionFromDistance(4.0), delta_error);
        assertEquals(0.0, calculator.getEmpiricalArmExtensionFromDistance(1.146302), delta_error);
        assertEquals(0.0, calculator.getEmpiricalArmExtensionFromDistance(1.0), delta_error);
        assertEquals(84.24, calculator.getEmpiricalArmExtensionFromDistance(5.0), delta_error);
        assertEquals(84.24, calculator.getEmpiricalArmExtensionFromDistance(10.0), delta_error);
    }
    @Test
    public void testGetArmAngleForExtension() {
        assertEquals(54.7, calculator.getArmAngleForExtension(0.0), .5);
        assertEquals(33.54, calculator.getArmAngleForExtension(50.8), .5);
        assertEquals(15.14, calculator.getArmAngleForExtension(101.6), .5);
        assertEquals(6.11, calculator.getArmAngleForExtension(127.0), .5);
        assertEquals(-13.25, calculator.getArmAngleForExtension(177.8), .5);
        assertEquals(-43.33, calculator.getArmAngleForExtension(241.3), .5);
    }
    @Test
    public void testGetArmExtensionForAngle() {
        assertEquals(0.0, calculator.getArmExtensionForAngle(54.7), .5);
        assertEquals(18.8, calculator.getArmExtensionForAngle(46.29), .5);
        assertEquals(67.3, calculator.getArmExtensionForAngle(27.56), .5);
        assertEquals(126.7, calculator.getArmExtensionForAngle(6.03), .5);
        assertEquals(165.0, calculator.getArmExtensionForAngle(-8.25), .5);
        assertEquals(191.0, calculator.getArmExtensionForAngle(-18.6), .5);
        assertEquals(216.0, calculator.getArmExtensionForAngle(-30), .5);
    }

    @Test
    public void testGetArmExtensionForDistance() {
        assertEquals(0.0, calculator.getArmExtensionForAngle(calculator.getArmSLAngleFromDistance(1.0)), .5);
        assertEquals(0.0, calculator.getArmExtensionForAngle(calculator.getArmSLAngleFromDistance(1.15)), .5);
        assertEquals(18.9, calculator.getArmExtensionForAngle(calculator.getArmSLAngleFromDistance(1.52)), .5);
        assertEquals(41.7, calculator.getArmExtensionForAngle(calculator.getArmSLAngleFromDistance(2.032)), .5);
        assertEquals(90.35, calculator.getArmExtensionForAngle(calculator.getArmSLAngleFromDistance(4.064)), .5);
    }
}
