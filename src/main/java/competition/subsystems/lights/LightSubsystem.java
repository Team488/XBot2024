

package competition.subsystems.lights;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XDigitalOutput;
import xbot.common.controls.actuators.XDigitalOutput.XDigitalOutputFactory;
import xbot.common.controls.actuators.XPWM.XPWMFactory;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.properties.StringProperty;

@Singleton
public class LightSubsystem extends BaseSubsystem {
    final XDigitalOutput dio0;
    final XDigitalOutput dio1;
    final XDigitalOutput dio2;
    final XDigitalOutput dio3;
    final XDigitalOutput dio4;
    final XDigitalOutput cubeDio;

    final XDigitalOutput[] dioOutputs;
    private int loopCounter;
    private final int loopMod = 5;
    private String chosenState;

    public enum LightsStateMessage{
        //at this time values are here as placeholders
        RobotNotBooted(1),
        RobotCollectingNote(22),
        RobotHoldingNote(30),
        RobotAuto(3),
        RobotEnabled(2),
        FacingSpeaker(4),
        FacingAmp(5),
        FacingSource(6);
        private int value;

        private LightsStateMessage(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }



    }
    @Inject
    public LightSubsystem(XDigitalOutputFactory digitalOutputFactory, XPWMFactory pwmFactory,
                          PropertyFactory pf, ElectricalContract contract) {
        dio0 = digitalOutputFactory.create(contract.getLightsDio0().channel);
        dio1 = digitalOutputFactory.create(contract.getLightsDio1().channel);
        dio2 = digitalOutputFactory.create(contract.getLightsDio2().channel);
        dio3 = digitalOutputFactory.create(contract.getLightsDio3().channel);
        dio4 = digitalOutputFactory.create(contract.getLightsDio4().channel);
        cubeDio = digitalOutputFactory.create(contract.getLightsCubeDio().channel);
        dioOutputs = new XDigitalOutput[] { dio0, dio1, dio2, dio3, dio4 };

        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Debug);
        chosenState = "Nothing Yet Set";
    }
    public void periodic() {
        LightsStateMessage currentState = LightsStateMessage.RobotNotBooted;

        int stateValue = currentState.getValue();
        for (int i = 0; i < dioOutputs.length; i++) {
            dioOutputs[i].set(((stateValue & (1 << i)) != 0));
        }

        aKitLog.record("ArduinoState", currentState.toString());
        aKitLog.record("DIO0", dio0.get());
        aKitLog.record("DIO1", dio1.get());
        aKitLog.record("DIO2", dio2.get());
        aKitLog.record("DIO3", dio3.get());
        aKitLog.record("DIO4", dio4.get());
        aKitLog.record("IsConeDIO", cubeDio.get());
    }
}
