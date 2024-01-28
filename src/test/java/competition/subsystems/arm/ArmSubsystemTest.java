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
        assertFalse(arm.hasSetTruePositionOffset);

        // Start at 0 with actual position of left: 150/limit,; and you move back to actual 0
        ((MockCANSparkMax)arm.armMotorLeft).setPosition(-150);

        // You hit the reverse limit switch and a periodic runs
        ((MockCANSparkMax)arm.armMotorLeft).setReverseLimitSwitchStateForTesting(true);
        arm.periodic();

        // Check offset, offset is the actual position when you had initially started
        assertEquals(150, arm.armMotorLeftRevolutionOffset.get(), 0.0001);
        assertTrue(arm.hasSetTruePositionOffset);
    }

    @Test
    public void testForwardLimitSwitch() {
        assertFalse(arm.hasSetTruePositionOffset);
        ((MockCANSparkMax)arm.armMotorLeft).setPosition(arm.armMotorRevolutionLimit.get() - 150);
        ((MockCANSparkMax)arm.armMotorLeft).setForwardLimitSwitchStateForTesting(true);
        arm.periodic();
        assertEquals(150, arm.armMotorLeftRevolutionOffset.get(), 0.0001);
        assertTrue(arm.hasSetTruePositionOffset);
    }

}
