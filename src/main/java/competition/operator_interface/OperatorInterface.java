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
    public XXboxController operatorGamepad;
    public XXboxController operatorGamepadSecond;

    public XXboxController autoGamepad;
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

        operatorGamepad = controllerFactory.create(1);
        operatorGamepad.setLeftInversion(false, true);
        operatorGamepad.setRightInversion(false, true);

        operatorGamepadSecond = controllerFactory.create(2);
        operatorGamepadSecond.setLeftInversion(false, true);
        operatorGamepadSecond.setRightInversion(false, true);

        autoGamepad = controllerFactory.create(3);

        neoTrellis = joystickFactory.create(2, 32);
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
