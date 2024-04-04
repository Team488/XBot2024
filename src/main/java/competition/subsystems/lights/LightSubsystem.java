

package competition.subsystems.lights;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XDigitalOutput;
import xbot.common.controls.actuators.XDigitalOutput.XDigitalOutputFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import java.util.Objects;

@Singleton
public class LightSubsystem extends BaseSubsystem {
    // based on the number of bits we have, this is the highest number we can send
    static final int numBits = 4;
    static final int maxValue = (int)(Math.pow(2, numBits) - 1);

    final AutonomousCommandSelector autonomousCommandSelector;
    final ShooterWheelSubsystem shooter;
    final CollectorSubsystem collector;
    final VisionSubsystem vision;
    final DynamicOracle oracle;
    final XDigitalOutput[] outputs;

    boolean ampSignalOn = false;

    public enum LightsStateMessage{
        NoCode(15), // we never send this one, it's implicit when the robot is off
        // and all of the DIOs float high
        DisabledCustomAutoSomeCamerasWorking(13),
        DisabledCustomAutoNoCamerasWorking(12),
        DisabledCustomAutoAllCamerasWorking(11),
        DisabledDefaultAutoSomeCamerasWorking(10),
        DisabledDefaultAutoNoCameraWorking(9),
        DisabledDefaultAutoAllCamerasWorking(8),
        RobotEnabled(5),
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
                          DynamicOracle oracle) {
        this.autonomousCommandSelector = autonomousCommandSelector;
        this.collector = collector;
        this.shooter = shooter;
        this.vision = vision;
        this.oracle = oracle;
        this.outputs = new XDigitalOutput[numBits];
        this.outputs[0] = digitalOutputFactory.create(contract.getLightsDio0().channel);
        this.outputs[1] = digitalOutputFactory.create(contract.getLightsDio1().channel);
        this.outputs[2] = digitalOutputFactory.create(contract.getLightsDio2().channel);
        this.outputs[3] = digitalOutputFactory.create(contract.getLightsDio3().channel);
        //this.pf = pf;
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

            }else {
                currentState = LightsStateMessage.RobotEnabled;
            }
        }
        return currentState;
    }

    public void sendState(LightsStateMessage state) {
        var bits = convertIntToBits(state.getValue());
        for(int i = 0; i < numBits; i++) {
            outputs[i].set(bits[i]);
        }
    }

    /**
     * Convert an integer to a boolean array representing the bits of the integer.
     * The leftmost bit in the result is the least significant bit of the integer.
     * This was chosen so we could add new bits onto the end of the array easily without changing
     * how earlier numbers were represented.
     * Eg: 
     * 0 -> [false, false, false, false]
     * 1 -> [true, false, false, false]
     * 14 -> [false, true, true, true]
     * 15 -> [true, true, true, true]
     */
    public static boolean[] convertIntToBits(int value) {
        boolean[] bits = new boolean[numBits];
        for(int i = 0; i < numBits; i++) {
            bits[i] = (value & (1 << i)) != 0;
        }
        return bits;
    }

    @Override
    public void periodic() {
        var currentState = getCurrentState();
        aKitLog.record("LightState", currentState.toString());
        sendState(currentState);

    }

    public void toggleAmpSignal() {
        ampSignalOn = !ampSignalOn;
    }
   
}
