package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;
import xbot.common.trajectory.XbotSwervePoint;

import java.util.ArrayList;

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
            SetRobotHeadingCommand resetHeading,
            SwerveSimpleTrajectoryCommand swerveTest)
    {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).onTrue(resetHeading);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).onTrue(pose.createCopyFusedOdometryToWheelsOdometryCommand());
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(pose.createSetPositionCommand(
                new Pose2d(2.6, 5.65, Rotation2d.fromDegrees(0))));

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(4.6, 5.65), Rotation2d.fromDegrees(0), 10));
        swerveTest.logic.setKeyPoints(points);
        swerveTest.logic.setEnableConstantVelocity(true);
        swerveTest.logic.setConstantVelocity(1);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).whileTrue(swerveTest);
    }
}
