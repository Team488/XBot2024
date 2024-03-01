

package competition.subsystems.lights;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XDigitalOutput;
import xbot.common.controls.actuators.XDigitalOutput.XDigitalOutputFactory;
import xbot.common.controls.actuators.XPWM.XPWMFactory;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.properties.StringProperty;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

@Singleton
public class LightSubsystem extends BaseSubsystem {

    SerialPort serialPort;
    private int loopCounter;
    private final int loopMod = 5;
    AutonomousCommandSelector autonomousCommandSelector;
    ShooterWheelSubsystem shooter;
    CollectorSubsystem collector;

    public enum LightsStateMessage{

        RobotNoCode("31"), // Likely to be unnecessary
        DisabledWithoutAuto("32"),
        DisabledWithAuto("33"),
        RobotEnabled("34"),
        AmpSignal("1"), // TODO: OI team please add a toggle!
        ReadyToShoot("2"),
        RobotContainsNote("3"),
        VisionSeesNote("4");

        LightsStateMessage(final String value) {
            this.value = value;
        }

        private String value;
        public String getValue() {
            return value;
        }
    }
    @Inject
    public LightSubsystem(PropertyFactory pf, AutonomousCommandSelector autonomousCommandSelector,
                          ShooterWheelSubsystem shooter, CollectorSubsystem collector) {
        serialPort = new SerialPort(115200, SerialPort.Port.kUSB, 64);
        // Not sure about these, is pf necessary here?
        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Debug);

        this.autonomousCommandSelector = autonomousCommandSelector;
        this.collector = collector;
        this.shooter = shooter;
    }

    @Override
    public void periodic() {
        // Runs period every 1/10 of a second
        if (this.loopCounter++ % loopMod != 0) {
            return;
        }

        boolean dsEnabled = DriverStation.isEnabled();
        LightsStateMessage currentState = LightsStateMessage.RobotNoCode;
        ShooterWheelTargetSpeeds shooterWheel = shooter.getCurrentValue();

        // Always assumes disabled without auto because we don't have AutonomousOracle >:(
        // Needs to implement vision as well
        // Not sure about if the way we are checking the shooter is correct
        if (!dsEnabled) {
//            if (autonomousCommandSelector.getCurrentAutonomousCommand() != null) {
//                if (oracle.isAutoCustomized()) {
//                    currentState = LightsStateMessage.RobotDisabledWithCustomizedAuto;
//                } else {
//                    currentState = LightsStateMessage.RobotDisabledWithBasicAuto;
//                }
//            } else {
//                currentState = LightsStateMessage.RobotDisabledNoAuto;
//            }
            currentState = LightsStateMessage.DisabledWithoutAuto;

        } else if (shooter.isMaintainerAtGoal() &&
                shooterWheel.lowerWheelsTargetRPM != 0 &&
                shooterWheel.upperWheelsTargetRPM != 0) {
            currentState = LightsStateMessage.ReadyToShoot;

        } else if (collector.getGamePieceReady()) {
            currentState = LightsStateMessage.RobotContainsNote;

        } else {
            currentState = LightsStateMessage.RobotEnabled;
        }

        String stateValue = currentState.getValue();

        // Write serial data to lights
        serialPort.writeString(stateValue + "\r\n");
        serialPort.flush();

        aKitLog.record("LightState", currentState.toString());
    }
}
