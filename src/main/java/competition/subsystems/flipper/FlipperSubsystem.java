package competition.subsystems.flipper;

import competition.electrical_contract.ElectricalContract;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XServo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FlipperSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    final XServo servo;
    final DoubleProperty inactivePosition;
    final DoubleProperty activePosition;
    private boolean active;

    @Inject
    public FlipperSubsystem(XServo.XServoFactory servoFactory, ElectricalContract contract,
                            PropertyFactory pf) {
        pf.setPrefix(this);
        inactivePosition = pf.createPersistentProperty("FlipperInactivePosition", 0.8);
        activePosition = pf.createPersistentProperty("FlipperActivePosition", 0.3);

        this.servo = servoFactory.create(contract.getFlipperServo().channel, getName() + "/Servo");

        active = false;
    }

    public void toggleServo() {
        System.out.println("Servo mode toggled.");
        active = !active;
        System.out.println("Mode: " + active);
        if (active) {
            servo.set(activePosition.get());
        } else {
            servo.set(inactivePosition.get());
        }
    }

    public void servoActive() {
        servo.set(activePosition.get());
        active = true;
    }

    public void servoInactive() {
        servo.set(inactivePosition.get());
        active = false;
    }

    @Override
    public void refreshDataFrame() {
        servo.refreshDataFrame();
    }

    public boolean getActive() {
        return active;
    }
}
