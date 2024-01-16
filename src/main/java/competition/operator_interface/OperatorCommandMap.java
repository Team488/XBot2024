package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.pose.PoseSubsystem;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;

/**
 * Maps operator interface buttons to commands
 */
@Singleton
public class OperatorCommandMap {

    @Inject
    public OperatorCommandMap() {}
    
    // Example for setting up a command to fire when a button is pressed:
    @Inject
    public void setupMyCommands(
            PoseSubsystem pose,
            OperatorInterface operatorInterface,
            SetRobotHeadingCommand resetHeading)
    {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getXboxButton(XXboxController.XboxButton.A).onTrue(resetHeading);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.B).onTrue(pose.getCopyFusedOdometryToWheelsOdometryCommand());
    }
}
