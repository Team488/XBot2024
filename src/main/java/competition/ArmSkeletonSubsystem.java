package competition;

import xbot.common.controls.actuators.XCANSparkMax;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ArmSkeletonSubsystem {

    public XCANSparkMax motor1;
    public XCANSparkMax motor2;
    public XboxController controller;

    // Arm movements
    double ARM_UP_SPEED = 0.5;
    double ARM_DOWN_SPEED = -0.5;
    double DESIRED_ANGLE = 45.0; // don't forget to change this when you want w

    public class ManualArmControlCommand extends CommandBase {
        @Override
        public void execute() {
            double joystickY = -controller.getLeftY();

            motor1.set(joystickY);
            motor2.set(joystickY);

            // Move the arm to a wanted angle
            double currentAngle = armEncoder();

            if (currentAngle < DESIRED_ANGLE) {
                // Move arm up
                motor1.set(ARM_UP_SPEED);
                motor2.set(ARM_UP_SPEED);
            } else if (currentAngle > DESIRED_ANGLE) {
                // Move arm down
                motor1.set(ARM_DOWN_SPEED);
                motor2.set(ARM_DOWN_SPEED);
            } else {
                // Stop the motors if the arm is good
                motor1.set(0.0);
                motor2.set(0.0);
            }
        }

        @Override
        public void end(boolean interrupted) {
            // Code to run when the command ends
        }

        @Override
        public boolean isFinished() {
            return false; // Modify this based on your command's end condition
        }
    }

    private double armEncoder() {
        // Implement your actual encoder reading logic here
        return 0;
    }
}