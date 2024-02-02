package competition.subsystems.arm;

import com.revrobotics.SparkLimitSwitch;
import competition.BaseCompetitionTest;
import org.junit.Test;
import xbot.common.controls.actuators.mock_adapters.MockCANSparkMax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ArmSubsystemTest extends BaseCompetitionTest {

    ArmSubsystem arm;

    @Override
    public void setUp() {
        super.setUp();
        arm = getInjectorComponent().armSubsystem();
    }

    private void checkMotorPower(double leftPower, double rightPower) {
        assertEquals(leftPower, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(rightPower, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);
    }

    private void checkMotorPower(double power) {
        checkMotorPower(power, power);
    }


    @Test
    public void testExtend() {
        assertNotEquals(arm.extendPower.get(), 0, 0.0001);
        checkMotorPower(0);
        arm.extend();
        checkMotorPower(arm.extendPower.get());
    }


    @Test
    public void testRetract() {
        assertNotEquals(arm.retractPower.get(), 0, 0.0001); // Check if retract power == 0
        checkMotorPower(0, 0); // Make sure motor not moving
        arm.retract();
        checkMotorPower(arm.retractPower.get());
    }


    @Test
    public void testStopped() {
        arm.extend();
        assertNotEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        arm.stop();
        checkMotorPower(0);
    }


    @Test
    public void testReverseLimitSwitch() {
        // Ensure that hasSetTruePositionOffset is false at start
        assertFalse(arm.hasCalibratedLeft);
        assertFalse(arm.hasCalibratedRight);

        // Start at 0 with actual position of left: 150/limit, right: 160/limit; and you move back to actual 0
        ((MockCANSparkMax)arm.armMotorLeft).setPosition(-150);
        ((MockCANSparkMax)arm.armMotorRight).setPosition(-160);

        // You hit the reverse limit switch and a periodic runs
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();

        // Check offset, offset is the actual position when you had initially started
        assertEquals(150, arm.armMotorLeftRevolutionOffset.get(), 0.0001);
        assertEquals(160, arm.armMotorRightRevolutionOffset.get(), 0.0001);
        assertTrue(arm.hasCalibratedLeft);
        assertTrue(arm.hasCalibratedRight);
    }


    @Test
    public void testForwardLimitSwitch() {
        assertFalse(arm.hasCalibratedLeft);
        assertFalse(arm.hasCalibratedRight);

        ((MockCANSparkMax)arm.armMotorLeft).setPosition(arm.armMotorRevolutionLimit.get() - 150);
        ((MockCANSparkMax)arm.armMotorRight).setPosition(arm.armMotorRevolutionLimit.get() - 160);

        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setForwardLimitSwitchStateForTesting(true);
        arm.periodic();

        assertEquals(150, arm.armMotorLeftRevolutionOffset.get(), 0.0001);
        assertEquals(160, arm.armMotorRightRevolutionOffset.get(), 0.0001);

        assertTrue(arm.hasCalibratedLeft);
        assertTrue(arm.hasCalibratedRight);
    }


    @Test
    public void testSoftLimit() {

        // The goal is: at limit, set power, check motor power

        // Both arm motors are static at first
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Left hit reverse limit!
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();

        // Tries to set power of both motors to -0.3, but result should be: left: 0, right: -0.3
        arm.setPower(-0.3);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(-0.3, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Reverse of above events
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(false);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();
        arm.setPower(-0.3);
        assertEquals(-0.3, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Left hits it again
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();
        arm.setPower(-0.3);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Reset
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(false);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(false);

        // New story: Left hit forward limit!
        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(true);
        arm.periodic();

        // Tries to set power of both motors to 0.3, but result should be: left: 0, right: 0.3
        arm.setPower(0.3);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0.3, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Now right hits the limit, left no longer at limit
        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(false);
        ((MockCANSparkMax)arm.armMotorRight).setForwardLimitSwitchStateForTesting(true);
        arm.periodic();
        arm.setPower(0.3);
        assertEquals(0.3, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Now left also hits forward limit
        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(true);
        arm.periodic();
        arm.setPower(0.2);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Reset
        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(false);
        ((MockCANSparkMax)arm.armMotorRight).setForwardLimitSwitchStateForTesting(false);
        arm.periodic();
        arm.setPower(0.2);

        assertEquals(0.2, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0.2, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        // Both limits left and right hit!
        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setForwardLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();

        arm.setPower(0.3);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);

        arm.setPower(-0.3);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(0, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);
    }
}
