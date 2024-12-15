package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.NeoTrellisGamepadSubsystem;
import competition.subsystems.pose.PointOfInterest;
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
    }

    public double getDriverGamepadTypicalDeadband() {
        return driverDeadband.get();
    }

    public double getOperatorGamepadTypicalDeadband() {
        return operatorDeadband.get();
    }

    public boolean getNeoTrellisValue(PointOfInterest pointOfInterest) {
        switch (pointOfInterest) {
            case CenterLine1:
                return neoTrellis.getButton(2);
            case CenterLine2:
                return neoTrellis.getButton(3);
            case CenterLine3:
                return neoTrellis.getButton(4);
            case CenterLine4:
                return neoTrellis.getButton(5);
            case CenterLine5:
                return neoTrellis.getButton(6);
            case SpikeTop:
                return neoTrellis.getButton(10);
            case SpikeMiddle:
                return neoTrellis.getButton(11);
            case SpikeBottom:
                return neoTrellis.getButton(12);
            case AmpFarScoringLocation:
                return neoTrellis.getButton(17);
            case TopSpikeCloserToSpeakerScoringLocation:
                return neoTrellis.getButton(18);
            case MiddleSpikeScoringLocation:
                return neoTrellis.getButton(19);
            case BottomSpikeCloserToSpeakerScoringLocation:
                return neoTrellis.getButton(20);
            case SubwooferTopScoringLocation:
                return neoTrellis.getButton(26);
            case SubwooferMiddleScoringLocation:
                return neoTrellis.getButton(27);
            case SubwooferBottomScoringLocation:
                return neoTrellis.getButton(28);
            default:
                break;
        }
        return false;
    }

    public void periodic() {
        this.driverGamepad.getRumbleManager().periodic();
        this.operatorGamepadAdvanced.getRumbleManager().periodic();
        this.operatorFundamentalsGamepad.getRumbleManager().periodic();
    }

}
