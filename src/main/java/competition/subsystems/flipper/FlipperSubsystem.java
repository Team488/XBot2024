package competition.subsystems.flipper;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XServo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FlipperSubsystem extends BaseSubsystem {

    final XServo leftServo;
    //final XServo rightServo;
    DoubleProperty inactivePosition;
    DoubleProperty activePosition;
    boolean active;

    @Inject
    public FlipperSubsystem(XServo.XServoFactory servoFactory, ElectricalContract contract,
                            PropertyFactory pf) {
        pf.setPrefix(this);
        inactivePosition = pf.createPersistentProperty("FlipperInactivePosition", 0);
        activePosition = pf.createPersistentProperty("FlipperActivePosition", 0.3);

        this.leftServo = servoFactory.create(contract.getFlipperServoLeft().channel);
        // this.rightServo = servoFactory.create(contract.getFlipperServoRight().channel);

        active = false;
    }

    public void toggleServo() {
        System.out.println("Servo mode toggled.");
        active = !active;
        System.out.println("Mode: " + active);
        if (active) {
            leftServo.set(0.51);
        } else {
            leftServo.set(0.16);
        }
    }
}
