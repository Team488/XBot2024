

package competition.subsystems.lights;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XDigitalOutput;
import xbot.common.controls.actuators.XDigitalOutput.XDigitalOutputFactory;
import xbot.common.controls.actuators.XPWM.XPWMFactory;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.properties.StringProperty;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
@Singleton
public class lightsSub extends BaseSubsystem {
    final XDigitalOutput dio0;
    final XDigitalOutput dio1;
    final XDigitalOutput dio2;
    final XDigitalOutput dio3;
    final XDigitalOutput dio4;
    final XDigitalOutput cubeDio;

    final XDigitalOutput[] dioOutputs;
    private final BooleanProperty dio0Property;
    private final BooleanProperty dio1Property;
    private final BooleanProperty dio2Property;
    private final BooleanProperty dio3Property;
    private final BooleanProperty dio4Property;
    private final BooleanProperty cubeDioProperty;

    public enum LightsStateMessage{
        //at this time values are here as placeholders
        RobotNotBooted(1),
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
    public lightsSub(XDigitalOutputFactory digitalOutputFactory, ElectricalContract contract, PropertyFactory pf) {
        dio0 = digitalOutputFactory.create(contract.getLightsDio0().channel);
        dio1 = digitalOutputFactory.create(contract.getLightsDio1().channel);
        dio2 = digitalOutputFactory.create(contract.getLightsDio2().channel);
        dio3 = digitalOutputFactory.create(contract.getLightsDio3().channel);
        dio4 = digitalOutputFactory.create(contract.getLightsDio4().channel);
        cubeDio = digitalOutputFactory.create(contract.getLightsCubeDio().channel);
        dioOutputs = new XDigitalOutput[] { dio0, dio1, dio2, dio3, dio4 };

        dio0Property = pf.createEphemeralProperty("DIO0", false);
        dio1Property = pf.createEphemeralProperty("DIO1", false);
        dio2Property = pf.createEphemeralProperty("DIO2", false);
        dio3Property = pf.createEphemeralProperty("DIO3", false);
        dio4Property = pf.createEphemeralProperty("DIO4", false);
        cubeDioProperty = pf.createEphemeralProperty("IsConeDIO", false);

    }
    public void periodic() {

        boolean dsEnabled = DriverStation.isEnabled();
        boolean isCube = arm.isCubeMode();

        cubeDio.set(isCube);
        dio0Property.set(dio0.get());
        dio1Property.set(dio1.get());
        dio2Property.set(dio2.get());
        dio3Property.set(dio3.get());
        dio4Property.set(dio4.get());
        cubeDioProperty.set(cubeDio.get());
    }
}
