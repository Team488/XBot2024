package competition.subsystems.flipper;

import competition.electrical_contract.ElectricalContract;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XServo;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FlipperSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    public final XServo servo;
    public final DoubleProperty inactivePosition;
    public final DoubleProperty activePosition;
    final DoubleProperty hangingPosition;
    public DoubleProperty flipperOffset;
    final XDigitalInput servoSensor;
    private boolean active;

    @Inject
    public FlipperSubsystem(XServo.XServoFactory servoFactory, ElectricalContract contract,
                            PropertyFactory pf, XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        pf.setPrefix(this);
        inactivePosition = pf.createPersistentProperty("FlipperInactivePosition", 0.8);
        activePosition = pf.createPersistentProperty("FlipperActivePosition", 0.3);
        hangingPosition = pf.createPersistentProperty("FlipperHangingPosition", 0.55);
        flipperOffset = pf.createPersistentProperty("FlipperOffset", 0);

        this.servo = servoFactory.create(contract.getFlipperServo().channel, getName() + "/Servo");
        this.servoSensor = xDigitalInputFactory.create(contract.getFlipperSensor(), this.getPrefix());

        active = false;
    }

    public void servoActive() {
        servo.set(activePosition.get() - flipperOffset.get());
        active = true;
    }

    public void servoInactive() {
        servo.set(inactivePosition.get() - flipperOffset.get());
        active = false;
    }

    public void servoToMax() {
        servo.set(1);
    }

    public void servoHangingPosition() {
        servo.set(hangingPosition.get());
    }

    @Override
    public void refreshDataFrame() {
        servo.refreshDataFrame();
    }

    public boolean getActive() {
        return active;
    }

    public boolean getFlipperSensorActivated() {
        return servoSensor.get();
    }
}