package competition.subsystems.flipper;

import competition.electrical_contract.ElectricalContract;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XDoubleSolenoid;
import xbot.common.controls.actuators.XServo;
import xbot.common.controls.actuators.XSolenoid;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FlipperSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    final XDoubleSolenoid flipDoubleSolenoid;
    final XServo servo;
    final DoubleProperty inactivePosition;
    final DoubleProperty activePosition;
    final DoubleProperty hangingPosition;
    private boolean active;

    @Inject
    public FlipperSubsystem(XSolenoid.XSolenoidFactory xSolenoidFactory, ElectricalContract contract,
                            PropertyFactory pf, XServo.XServoFactory servoFactory) {
        pf.setPrefix(this);

        inactivePosition = pf.createPersistentProperty("FlipperInactivePosition", 0.8);
        activePosition = pf.createPersistentProperty("FlipperActivePosition", 0.3);
        hangingPosition = pf.createPersistentProperty("FlipperHangingPosition", 0.55);

        var flipperSolenoid1 = xSolenoidFactory.create(contract.getFlipperSolenoidForward().channel);
        var flipperSolenoid2 = xSolenoidFactory.create(contract.getFlipperSolenoidReverse().channel);
        this.flipDoubleSolenoid = new XDoubleSolenoid(flipperSolenoid1, flipperSolenoid2);
        this.servo = servoFactory.create(contract.getFlipperServo().channel, getName() + "/Servo");

        active = false;
    }

    public void setSolenoid(boolean active) {
        if (active) {
            flipDoubleSolenoid.setForward();
        } else {
            flipDoubleSolenoid.setReverse();
        }
    }

    public void flipperActive() {
        active = true;
        servo.set(activePosition.get());
        setSolenoid(active);
    }

    public void flipperInactive() {
        active = false;
        servo.set(inactivePosition.get());
        setSolenoid(active);
    }

    public void flipperServoHangingPosition() {
        servo.set(hangingPosition.get());
    }

    @Override
    public void refreshDataFrame() {
        servo.refreshDataFrame();
    }

    public boolean getActive() {
        return active;
    }
}