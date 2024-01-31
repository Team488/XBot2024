package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.commands.SwerveAccordingToOracleCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;
import xbot.common.trajectory.LowResField;
import xbot.common.trajectory.Obstacle;
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
            SwerveSimpleTrajectoryCommand swerveTest,
            SwerveSimpleTrajectoryCommand avoidColumnTest,
            SwerveAccordingToOracleCommand oracleSwerve,
            ManualRobotKnowledgeSubsystem knowledgeSubsystem,
            DynamicOracle oracle)
    {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).onTrue(resetHeading);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).onTrue(pose.createSetPositionCommand(
                new Pose2d(2.760, 5.396, Rotation2d.fromDegrees(90))));

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Back).onTrue(swerveTest);

        ArrayList<XbotSwervePoint> pointsB = new ArrayList<>();
        pointsB.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(1, 3), Rotation2d.fromDegrees(0), 10));
        swerveTest.logic.setKeyPoints(pointsB);
        swerveTest.logic.setEnableConstantVelocity(true);
        swerveTest.logic.setConstantVelocity(1);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).onTrue(swerveTest);


        ArrayList<XbotSwervePoint> pointsY = new ArrayList<>();
        pointsY.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(2.760, 5.396), Rotation2d.fromDegrees(0), 10));
        swerveTest.logic.setKeyPoints(pointsY);
        swerveTest.logic.setEnableConstantVelocity(true);
        swerveTest.logic.setConstantVelocity(1);



        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(swerveTest);


        ArrayList<XbotSwervePoint> pointsSt = new ArrayList<>();
        pointsSt.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(1.5, 5.5), Rotation2d.fromDegrees(90), 10));
        swerveTest.logic.setKeyPoints(pointsSt);
        swerveTest.logic.setEnableConstantVelocity(true);
        swerveTest.logic.setConstantVelocity(1);


    }
}
