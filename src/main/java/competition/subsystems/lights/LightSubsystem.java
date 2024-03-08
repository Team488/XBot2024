

package competition.subsystems.lights;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import xbot.common.command.BaseSubsystem;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

@Singleton
public class LightSubsystem extends BaseSubsystem {

    AutonomousCommandSelector autonomousCommandSelector;
    ShooterWheelSubsystem shooter;
    CollectorSubsystem collector;
    SerialPort serialPort;
    private int loopcount = 1;
    private final int loopMod = 4;
    public boolean ampSignalOn = false;
    public boolean lightsWorking = false;

    public enum LightsStateMessage{
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
    public LightSubsystem(AutonomousCommandSelector autonomousCommandSelector,
                          ShooterWheelSubsystem shooter, CollectorSubsystem collector) {

        if(USBIsNotConnected(SerialPort.Port.kUSB1)) {
            if(USBIsNotConnected(SerialPort.Port.kUSB2)) {
                if(USBIsNotConnected(SerialPort.Port.kUSB)) {
                    log.error("Lights not working");
                }
            }
        }
        // the default timeout is 5s, set a much smaller value
        serialPort.setTimeout(0.05);
        this.autonomousCommandSelector = autonomousCommandSelector;
        this.collector = collector;
        this.shooter = shooter;
    }

    public boolean USBIsNotConnected(SerialPort.Port port) {
        try {
            serialPort = new SerialPort(9600, port, 8);
            serialPort.setWriteBufferMode(SerialPort.WriteBufferMode.kFlushOnAccess);
            return false;
        }
        catch (Exception e) {
            log.error("Lights not working: %s", e);
            return true;
        }
    }

    @Override
    public void periodic() {
        if (!lightsWorking) {
            return;
        }

        try {
            serialPort.reset();
            // Runs period every 1/10 of a second
            if (this.loopcount++ % loopMod != 0) {
                return;
            }

            boolean dsEnabled = DriverStation.isEnabled();
            LightsStateMessage currentState;
            ShooterWheelTargetSpeeds shooterWheel = shooter.getCurrentValue();

            // Needs to implement vision as well
            // Not sure about if the way we are checking the shooter is correct (and collector)
            if (!dsEnabled) {
                // Check if auto program is set
                if (autonomousCommandSelector.getCurrentAutonomousCommand() != null) {
                    currentState = LightsStateMessage.DisabledWithAuto;
                } else {
                    currentState = LightsStateMessage.DisabledWithoutAuto;
                }

            } else {
                // Try and match enabled states
                if (ampSignalOn) {
                    currentState = LightsStateMessage.AmpSignal;

                } else if (shooter.isReadyToFire()) {
                    currentState = LightsStateMessage.ReadyToShoot;

                } else if (collector.getGamePieceReady()) {
                    currentState = LightsStateMessage.RobotContainsNote;

                } else {
                    currentState = LightsStateMessage.RobotEnabled;
                }
            }

            String stateValue = currentState.getValue();

        // Write serial data to lights
        serialPort.writeString(stateValue + "\n");
        //serialPort.flush();

            aKitLog.record("LightState", currentState.toString());
        } catch (Exception e) {
            log.info("There is a problem within LightSubsystem.java. Exception: " + e.toString());
        }
    }
}
