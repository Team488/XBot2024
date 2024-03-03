package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.NeoTrellisGamepadSubsystem;
import xbot.common.controls.sensors.XJoystick;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.controls.sensors.XXboxController.XXboxControllerFactory;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

/**
 * This class is the glue that binds the controls on the physical operator interface to the commands and command groups
 * that allow control of the robot.
 */
@Singleton
public class OperatorInterface {

    // ONE GAMEPAD IS FOR COMPETITION, SECOND GAMEPAD IS USED DURING PRACTICE
    public XXboxController driverGamepad;
    public XXboxController operatorFundamentalsGamepad;
    public XXboxController operatorGamepadAdvanced;

    public XJoystick neoTrellis;
    public NeoTrellisGamepadSubsystem neoTrellisLights;

    final DoubleProperty driverDeadband;
    final DoubleProperty operatorDeadband;
    final DoubleProperty operatorDeadbandSecond;

    @Inject
    public OperatorInterface(XXboxControllerFactory controllerFactory,
                             XJoystick.XJoystickFactory joystickFactory,
                             NeoTrellisGamepadSubsystem neoTrellisSubsystem,
                             RobotAssertionManager assertionManager,
                             PropertyFactory pf) {
        driverGamepad = controllerFactory.create(0);
        driverGamepad.setLeftInversion(false, true);
        driverGamepad.setRightInversion(true, true);

        operatorGamepadAdvanced = controllerFactory.create(1);
        operatorGamepadAdvanced.setLeftInversion(false, true);
        operatorGamepadAdvanced.setRightInversion(false, true);

        operatorFundamentalsGamepad = controllerFactory.create(2);
        operatorFundamentalsGamepad.setLeftInversion(false, true);
        operatorFundamentalsGamepad.setRightInversion(false, true);

        neoTrellis = joystickFactory.create(3, 32);
        neoTrellisLights = neoTrellisSubsystem;

        pf.setPrefix("OperatorInterface");
        driverDeadband = pf.createPersistentProperty("Driver Deadband", 0.12);
        operatorDeadband = pf.createPersistentProperty("Operator Deadband", 0.15);

        operatorDeadbandSecond = pf.createPersistentProperty("Operator Deadband Second", 0.15);
    }

    public double getDriverGamepadTypicalDeadband() {
        return driverDeadband.get();
    }

    public double getOperatorGamepadTypicalDeadband() {
        return operatorDeadband.get();
    }

    public double getOperatorGamepadTypicalDeadbandSecond() {
        return operatorDeadbandSecond.get();
    }
}
