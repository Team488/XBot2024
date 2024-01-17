package competition.subsystems.drive.commands;

import edu.wpi.first.wpilibj.XboxController;
public class RobotArm2024 {


    motor motor1;
    motor motor2;
    private XboxController controller;
    public double armEncoder;


    //        Arm movements
    private static final double ARM_UP_SPEED = 0.5;
    private static final double ARM_DOWN_SPEED = -0.5;
    private static final double DESIRED_ANGLE = 45.0; // don't forget to change this when you want w



    public void initialize() {

        motor1 = new motor();
        motor2 = new motor();
        controller = new XboxController(0);

        double joystickY = -controller.getLeftY();


        motor1.set(joystickY);
        motor2.set(joystickY);
        // Move the arm to a wanted angle
        double currentAngle;
        currentAngle = armEncoder();
        if (currentAngle < DESIRED_ANGLE) {
            // Move arm up
            motor1.set(ARM_UP_SPEED);
            motor2.set(ARM_UP_SPEED);
        } else if (currentAngle > DESIRED_ANGLE) {
            // Move arm down
            motor1.set(ARM_DOWN_SPEED);
            motor2.set(ARM_DOWN_SPEED);
        } else {
            // Stop the motors if the arm is goody
            motor1.set(0.0);
            motor2.set(0.0);
        }

    }

    private double armEncoder() {
        return 0;
    }

}