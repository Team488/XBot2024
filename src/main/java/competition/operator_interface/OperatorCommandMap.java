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
import xbot.common.math.XYPair;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;
import xbot.common.trajectory.LowResField;
import xbot.common.trajectory.Obstacle;
import xbot.common.trajectory.XbotSwervePoint;

import java.util.ArrayList;
import java.lang.Math;

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
            SwerveSimpleTrajectoryCommand pointAtSpeaker,
            SwerveAccordingToOracleCommand oracleSwerve,
            ManualRobotKnowledgeSubsystem knowledgeSubsystem,
            DynamicOracle oracle)
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

        ArrayList<XbotSwervePoint> columnPoints = new ArrayList<>();
        columnPoints.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(4.5, 5), Rotation2d.fromDegrees(170), 10));
        avoidColumnTest.logic.setKeyPoints(columnPoints);
        avoidColumnTest.logic.setEnableConstantVelocity(true);
        avoidColumnTest.logic.setConstantVelocity(2);
        avoidColumnTest.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).whileTrue(avoidColumnTest);

        ArrayList<XbotSwervePoint> speakerPoints = new ArrayList<>();
        speakerPoints.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(5.7,6.3), Rotation2d.fromDegrees(190), 10));
        speakerPoints.add(XbotSwervePoint.createXbotSwervePoint(
                new Translation2d(3.7,2.429256), Rotation2d.fromDegrees(132), 10));
        pointAtSpeaker.logic.setKeyPoints(speakerPoints);
        pointAtSpeaker.logic.setEnableConstantVelocity(true);
        pointAtSpeaker.logic.setConstantVelocity(2);
        pointAtSpeaker.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());

        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).whileTrue(pointAtSpeaker);

        oracleSwerve.logic.setEnableConstantVelocity(true);
        oracleSwerve.logic.setConstantVelocity(3);
        oracleSwerve.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(oracleSwerve);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftBumper)
                .whileTrue(knowledgeSubsystem.createSetNoteCollectedCommand());
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightBumper)
                .whileTrue(knowledgeSubsystem.createSetNoteShotCommand());
    }
}
