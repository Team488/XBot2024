package competition.subsystems.arm;

import competition.BaseCompetitionTest;
import org.junit.Test;
import xbot.common.controls.actuators.mock_adapters.MockCANSparkMax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
// Expect, Actual

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

    private void checkLimitState(ArmSubsystem.LimitState limitState) {
        assertEquals(arm.getLimitState(arm.armMotorLeft), limitState);
        assertEquals(arm.getLimitState(arm.armMotorRight), limitState);
    }

    private void setMotorPosition(double position) {
        arm.armMotorLeft.setPosition(position);
        arm.armMotorRight.setPosition(position);
    }
  

    @Test
    public void testExtend() {
        // Assuming arm motors has already calibrated
        arm.hasCalibratedLeft = true;
        arm.hasCalibratedRight = true;

        assertNotEquals(arm.extendPower.get(), 0, 0.0001);
        checkMotorPower(0);
        arm.extend();
        checkMotorPower(arm.extendPower.get());
    }


    @Test
    public void testRetract() {
        arm.hasCalibratedLeft = true;
        arm.hasCalibratedRight = true;

        assertNotEquals(arm.retractPower.get(), 0, 0.0001); // Check if retract power == 0
        checkMotorPower(0); // Make sure motor not moving
        arm.retract();
        checkMotorPower(arm.retractPower.get());
    }


    @Test
    public void testStopped() {
        arm.hasCalibratedLeft = true;
        arm.hasCalibratedRight = true;

        checkMotorPower(0);
        arm.extend();
        assertNotEquals(0, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        arm.stop();
        checkMotorPower(0);
    }


    @Test
    public void testReverseLimitOffset() {
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
    public void testLimits() {

        // Check both arms are not yet calibrated and are static
        assertFalse(arm.hasCalibratedLeft);
        assertFalse(arm.hasCalibratedRight);

        checkMotorPower(0);

        // Attempt to move forward but should remain static
        arm.setPower(0.3);
        checkMotorPower(0);

        // Attempt to move backward but should only be moving at -0.1
        arm.setPower(-0.3);
        checkMotorPower(-0.1);

        // Reverse limit hit (Usually you only get here for calibration)
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();
        checkLimitState(ArmSubsystem.LimitState.LOWER_LIMIT_HIT);

        assertTrue(arm.hasCalibratedLeft);
        assertTrue(arm.hasCalibratedRight);

        arm.setPower(-0.3);
        checkMotorPower(0);

        arm.setPower(0.3);
        checkMotorPower(0.3);

        // Test LimitState: UPPER_LIMIT_HIT (Usually you can't get here)

        // A hacky way to bypass "Soft-limits" since we only want to test when actually AT limit
        setMotorPosition(5000);

        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(false);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(false);

        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setForwardLimitSwitchStateForTesting(true);
        arm.periodic();
        checkLimitState(ArmSubsystem.LimitState.UPPER_LIMIT_HIT);

        arm.setPower(0.3);
        checkMotorPower(0);

        arm.setPower(-0.3);
        checkMotorPower(-0.3);

        // Test LimitState: BOTH_LIMITS_HIT (Activates when there is issues...)
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();
        checkLimitState(ArmSubsystem.LimitState.BOTH_LIMITS_HIT);

        arm.setPower(0.3);
        checkMotorPower(0);

        arm.setPower(-0.3);
        checkMotorPower(0);
    }


    @Test
    public void testGetArmAngleFromDistance() {
        // In case if getArmAngleFromDistance for whatever reason is wayyyyyy off
        // (Will likely fail is the equation is changed)
        assertEquals(52, arm.getArmAngleFromDistance(50), 5);
        assertEquals(31, arm.getArmAngleFromDistance(100), 5);
        assertEquals(20, arm.getArmAngleFromDistance(150), 5);
    }
}
