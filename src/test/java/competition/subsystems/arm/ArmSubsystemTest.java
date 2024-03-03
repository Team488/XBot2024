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
        arm.setClampLimit(1.0);
        arm.setRampingPowerEnabled(false);
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
        arm.markArmsAsCalibratedAgainstLowerPhyscalLimit();

        ((MockCANSparkMax)arm.armMotorLeft).setPosition(20);
        ((MockCANSparkMax)arm.armMotorRight).setPosition(20);

        assertNotEquals(arm.extendPower, 0, 0.0001);
        checkMotorPower(0);
        arm.extend();
        checkMotorPower(arm.extendPower);
    }


    @Test
    public void testRetract() {
        arm.hasCalibratedLeft = true;
        arm.hasCalibratedRight = true;
        arm.markArmsAsCalibratedAgainstLowerPhyscalLimit();

        ((MockCANSparkMax)arm.armMotorLeft).setPosition(20);
        ((MockCANSparkMax)arm.armMotorRight).setPosition(20);

        assertNotEquals(arm.retractPower, 0, 0.0001); // Check if retract power == 0
        checkMotorPower(0); // Make sure motor not moving
        arm.retract();
        checkMotorPower(arm.retractPower);
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
        arm.refreshDataFrame();
        arm.periodic();

        // Check offset, offset is the actual position when you had initially started
        assertEquals(150, arm.getLeftArmOffset(), 0.0001);
        assertEquals(160, arm.getRightArmOffset(), 0.0001);
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
        checkMotorPower(arm.lowerExtremelySlowZonePowerLimit.get());

        // Reverse limit hit (Usually you only get here for calibration)
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.refreshDataFrame();
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
        arm.refreshDataFrame();
        arm.periodic();
        checkLimitState(ArmSubsystem.LimitState.UPPER_LIMIT_HIT);

        arm.setPower(0.3);
        checkMotorPower(0);

        arm.setPower(arm.powerMin.get());
        checkMotorPower(arm.powerMin.get());

        // Test LimitState: BOTH_LIMITS_HIT (Activates when there is issues...)
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        ((MockCANSparkMax)arm.armMotorRight).setReverseLimitSwitchStateForTesting(true);
        arm.refreshDataFrame();
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
        assertEquals(54.7, arm.getArmAngleFromDistance(1.0), .5);
        assertEquals(54.7, arm.getArmAngleFromDistance(1.15), .5);
        assertEquals(44.76, arm.getArmAngleFromDistance(1.6), .5);
        assertEquals(37.17, arm.getArmAngleFromDistance(2.032), .5);
        assertEquals(27.3, arm.getArmAngleFromDistance(2.8194), .5);
        assertEquals(12.23, arm.getArmAngleFromDistance(6.09), .5);
    }
    @Test
    public void testGetArmAngleForExtension() {
        assertEquals(54.7, arm.getArmAngleForExtension(0.0), .5);
        assertEquals(33.54, arm.getArmAngleForExtension(50.8), .5);
        assertEquals(15.14, arm.getArmAngleForExtension(101.6), .5);
        assertEquals(6.11, arm.getArmAngleForExtension(127.0), .5);
        assertEquals(-13.25, arm.getArmAngleForExtension(177.8), .5);
        assertEquals(-43.33, arm.getArmAngleForExtension(241.3), .5);

    }
    @Test
    public void testGetArmExtensionForAngle() {
        assertEquals(0.0, arm.getArmExtensionForAngle(54.7), .5);
        assertEquals(18.8, arm.getArmExtensionForAngle(46.29), .5);
        assertEquals(67.3, arm.getArmExtensionForAngle(27.56), .5);
        assertEquals(126.7, arm.getArmExtensionForAngle(6.03), .5);
        assertEquals(165.0, arm.getArmExtensionForAngle(-8.25), .5);
        assertEquals(191.0, arm.getArmExtensionForAngle(-18.6), .5);
        assertEquals(216.0, arm.getArmExtensionForAngle(-30), .5);
        assertEquals(arm.upperLegalLimitMm.get(), arm.getArmExtensionForAngle(-43.6), .5);

    }

    @Test
    public void testGetArmExtensionForDistance() {
        assertEquals(0.0, arm.getArmExtensionForAngle(arm.getArmAngleFromDistance(1.0)), .5);
        assertEquals(0.0, arm.getArmExtensionForAngle(arm.getArmAngleFromDistance(1.15)), .5);
        assertEquals(18.9, arm.getArmExtensionForAngle(arm.getArmAngleFromDistance(1.52)), .5);
        assertEquals(41.7, arm.getArmExtensionForAngle(arm.getArmAngleFromDistance(2.032)), .5);
        assertEquals(90.35, arm.getArmExtensionForAngle(arm.getArmAngleFromDistance(4.064)), .5);


    }
}