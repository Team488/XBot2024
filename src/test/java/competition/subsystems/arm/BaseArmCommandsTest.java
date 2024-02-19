package competition.subsystems.arm;

import competition.BaseCompetitionTest;
import competition.operator_interface.OperatorCommandMap;
import competition.subsystems.arm.commands.ExtendArmCommand;
import competition.subsystems.arm.commands.ReconcileArmAlignmentCommand;
import competition.subsystems.arm.commands.RetractArmCommand;
import competition.subsystems.arm.commands.StopArmCommand;
import org.junit.Test;
import xbot.common.controls.actuators.mock_adapters.MockCANSparkMax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BaseArmCommandsTest extends BaseCompetitionTest {

    ArmSubsystem arm;
    ExtendArmCommand extend;
    RetractArmCommand retract;
    StopArmCommand stop;
    ReconcileArmAlignmentCommand reconcile;

    @Override
    public void setUp() {
        super.setUp();
        arm = getInjectorComponent().armSubsystem();
        extend = getInjectorComponent().extendArmCommand();
        retract = getInjectorComponent().retractArmCommand();
        stop = getInjectorComponent().stopArmCommand();
        reconcile = getInjectorComponent().reconcileArmAlignmentCommand();
        arm.setClampLimit(1.0);
    }

    private void checkMotorPower(double power) {
        assertEquals(power, ((MockCANSparkMax)arm.armMotorLeft).get(), 0.0001);
        assertEquals(power, ((MockCANSparkMax)arm.armMotorRight).get(), 0.0001);
    }

    @Test
    public void test() {
        arm.hasCalibratedLeft = true;
        arm.hasCalibratedRight = true;

        // Start at zero
        checkMotorPower(0);

        // Extend
        extend.initialize();
        extend.execute();
        checkMotorPower(arm.extendPower);

        // Retract
        retract.initialize();
        retract.execute();
        checkMotorPower(arm.softTerminalLowerLimitSpeed.get());

        // Stop
        stop.initialize();
        stop.execute();
        checkMotorPower(0);

        // Reconcile
        reconcile.initialize();
        reconcile.execute();
        assertNotEquals(arm.armMotorLeft.get(), arm.armMotorRight.get());
    }
}
