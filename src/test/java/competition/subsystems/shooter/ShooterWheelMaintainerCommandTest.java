package competition.subsystems.shooter;

import competition.BaseCompetitionTest;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.shooter.commands.ShooterWheelMaintainerCommand;
import org.junit.Test;
import xbot.common.controls.actuators.mock_adapters.MockCANSparkMax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShooterWheelMaintainerCommandTest extends BaseCompetitionTest {

    ShooterWheelMaintainerCommand command;
    ShooterWheelSubsystem wheel;
    ArmSubsystem arm;

    @Override
    public void setUp() {
        super.setUp();
        command = this.getInjectorComponent().shooterWheelMaintainerCommand();
        wheel = this.getInjectorComponent().shooterSubsystem();
        arm = this.getInjectorComponent().armSubsystem();
    }

    @Test
    public void testDifferentThresholds() {
        command.initialize();
        command.execute();

        wheel.setTargetValue(3000);
        setWheelSpeed(2500);
        moveArm(15);
        wheel.refreshDataFrame();
        arm.refreshDataFrame();

        assertFalse(command.getErrorWithinTolerance());

        setWheelSpeed(2500);
        moveArm(0);
        wheel.refreshDataFrame();
        arm.refreshDataFrame();

        assertTrue(command.getErrorWithinTolerance());

        setWheelSpeed(2500);
        moveArm(1000);
        wheel.refreshDataFrame();
        arm.refreshDataFrame();

        assertTrue(command.getErrorWithinTolerance());
    }


    private void setWheelSpeed(double speed) {
        ((MockCANSparkMax)wheel.upperWheelMotor).setVelocity(speed);
        ((MockCANSparkMax)wheel.lowerWheelMotor).setVelocity(speed);
    }

    private void moveArm(double position) {
        ((MockCANSparkMax)arm.armMotorLeft).setPosition(position);
        ((MockCANSparkMax)arm.armMotorRight).setPosition(position);
    }
}
