package competition.subsystems.lights;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XDigitalOutput.XDigitalOutputFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import java.util.Objects;

@Singleton
public class LightSubsystem extends BaseSubsystem {
    // based on the number of bits we have, this is the highest number we can send
    static final int numBits = 6;
    static final int maxValue = (int)(Math.pow(2, numBits) - 1);

    final AutonomousCommandSelector autonomousCommandSelector;
    final ShooterWheelSubsystem shooter;
    final CollectorSubsystem collector;
    final VisionSubsystem vision;
    final DynamicOracle oracle;

    SerialPort serialPort;
    private int loopcount = 1;
    private final int loopMod = 4;
    public boolean lightsWorking = false;

    boolean ampSignalOn = false;

    public enum LightsStateMessage{
        NoCode(15),
        DisabledCustomAutoSomeCamerasWorking(13),
        DisabledCustomAutoNoCamerasWorking(12),
        DisabledCustomAutoAllCamerasWorking(11),
        DisabledDefaultAutoSomeCamerasWorking(10),
        DisabledDefaultAutoNoCameraWorking(2),
        DisabledDefaultAutoAllCamerasWorking(8),
        RobotEnabled(34),
        ShooterReadyWithoutNote(1),
        ReadyToShoot(2),
        RobotContainsNote(3),
        CenterCamSeesNotes(4),
        SideCamsSeeNotes(7);

        LightsStateMessage(final int value) {
            if(value > maxValue || value < 0) {
                // it should be okay to have this throw because it will happen immediately on robot startup
                // so we'll see failures here in CI before deploying to the robot.
                // Getting the RobotAssertionManager in here was proving tricky
                System.out.println("Values must be between 0 and " + maxValue + " inclusive. Got " + value + " instead. Will always return 0 for safety.");
            }
            this.value = value;
        }

        private int value;
        public int getValue() {
            if (value < 0 || value > maxValue) {
                return 0;
            }
            return value;
        }

        public static LightsStateMessage getStringValueFromInt(int i) {
            for (LightsStateMessage states : LightsStateMessage.values()) {
                if (states.getValue() == i) {
                    return states;
                }
            }
           return LightsStateMessage.NoCode;
        }
    }

    @Inject
    public LightSubsystem(XDigitalOutputFactory digitalOutputFactory,
                          ElectricalContract contract,
                          AutonomousCommandSelector autonomousCommandSelector,
                          ShooterWheelSubsystem shooter, CollectorSubsystem collector,
                          VisionSubsystem vision,
                          DynamicOracle oracle, OperatorInterface oi) {
        this.autonomousCommandSelector = autonomousCommandSelector;
        this.collector = collector;
        this.shooter = shooter;
        this.vision = vision;
        this.oracle = oracle;

        // Connect to USB port over serial
        if (usbIsNotConnected(SerialPort.Port.kUSB1)) { // Top port should map to kUSB1. Bottom port is for USB drive
            log.error("Lights - could not find a valid USB serial port.");
        } else { // when correct SerialPort is found
            serialPort.setTimeout(0.05);
        }
    }

    public LightsStateMessage getCurrentState() {
        boolean dsEnabled = DriverStation.isEnabled();
        LightsStateMessage currentState;

        // Needs to implement vision as well
        // Not sure about if the way we are checking the shooter is correct (and collector)
        if (!dsEnabled) {
            // Check if auto program is set
            int base = 8;
            if (!Objects.equals(autonomousCommandSelector.getProgramName(), "SubwooferShotFromMidShootThenShootNearestThree")) {
                // Not default
                base = 11;
            }
            // 0 as no camera working, 1 as all camera working, 2 as some camera working
            currentState = LightsStateMessage.getStringValueFromInt(base + vision.cameraWorkingState());
        } else {
            if (shooter.isReadyToFire() && collector.checkSensorForLights()) {
                currentState = LightsStateMessage.ReadyToShoot;

            } else if (collector.checkSensorForLights()) {
                currentState = LightsStateMessage.RobotContainsNote;

            } else if (shooter.isReadyToFire()) {
                currentState = LightsStateMessage.ShooterReadyWithoutNote;

            } else if (vision.checkIfCenterCamSeesNote()) {
                currentState = LightsStateMessage.CenterCamSeesNotes;

            } else if (vision.checkIfSideCamsSeeNote()) {
                currentState = LightsStateMessage.SideCamsSeeNotes;

            } else {
                currentState = LightsStateMessage.RobotEnabled;
            }
        }
        return currentState;
    }

    public static boolean[] convertIntToBits(int value) {
        boolean[] bits = new boolean[numBits];
        for(int i = 0; i < numBits; i++) {
            bits[i] = (value & (1 << i)) != 0;
        }
        return bits;
    }

    public boolean usbIsNotConnected(SerialPort.Port port) {
        try {
            serialPort = new SerialPort(9600, port, 8);
            serialPort.setWriteBufferMode(SerialPort.WriteBufferMode.kFlushOnAccess);
            lightsWorking = true;
            return false;
        } catch (Exception e) {
            log.error("Lights are not working: %s", e);
            return true;
        }
    }

    @Override
    public void periodic() {
        var currentState = getCurrentState();
        aKitLog.record("LightState", currentState.toString());

        // Try sending over serial
        if (!lightsWorking) {
            return;
        }
        try {
            serialPort.reset();

            // run every 1/10th of a second
            if (this.loopcount++ % loopMod != 0) {
                return;
            }

            // Write serial data to lights
            String stateValue = String.valueOf(currentState.getValue());
            serialPort.writeString(stateValue + "\n");
            serialPort.flush();
        } catch (Exception e) {
            log.info("problem occurred within LightSubsystem " + e.toString());
        }
    }

    public void toggleAmpSignal() {
        ampSignalOn = !ampSignalOn;
    }

}
