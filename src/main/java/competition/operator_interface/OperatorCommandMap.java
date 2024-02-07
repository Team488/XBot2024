package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;


import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;
import xbot.common.trajectory.SwerveSimpleTrajectoryLogic;
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
            SwerveSimpleTrajectoryCommand avoidColumnTest,
            ManualRobotKnowledgeSubsystem knowledgeSubsystem,
            DynamicOracle oracle,
            SwerveSimpleTrajectoryCommand bottomRing,
            SwerveSimpleTrajectoryCommand middleRing,
            SwerveSimpleTrajectoryCommand topRing,
            SwerveSimpleTrajectoryCommand startMiddleRing,
            SwerveSimpleTrajectoryCommand startTRing,
            SwerveSimpleTrajectoryCommand Start

    )


    {
        // Set up top ring goal position

        // Bind topRing to a button


        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).whileTrue(resetHeading);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).whileTrue(startTRing);

        ArrayList<XbotSwervePoint> pointsBa = new ArrayList<>();
        pointsBa.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(3, 7), Rotation2d.fromDegrees(45), 10));
        startTRing.logic.setKeyPoints(pointsBa);
        startTRing.logic.setEnableConstantVelocity(true);
        startTRing.logic.setConstantVelocity(1);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(startTRing);

        ArrayList<XbotSwervePoint> pointsTR = new ArrayList<>();
        pointsTR.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(3, 5), Rotation2d.fromDegrees(45), 10));
        startTRing.logic.setKeyPoints(pointsBa);
        startTRing.logic.setEnableConstantVelocity(true);
        startTRing.logic.setConstantVelocity(1);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).whileTrue(startMiddleRing);


        ArrayList<XbotSwervePoint> pointsY = new ArrayList<>();
        pointsY.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(2.760, 5.396), Rotation2d.fromDegrees(0), 10));
        startMiddleRing.logic.setKeyPoints(pointsY);
        startMiddleRing.logic.setEnableConstantVelocity(true);
        startMiddleRing.logic.setConstantVelocity(1);



        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).whileTrue(bottomRing);


        ArrayList<XbotSwervePoint> pointsB = new ArrayList<>();
        pointsB.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(8.2, 0.7), Rotation2d.fromDegrees(0), 10));
        bottomRing.logic.setKeyPoints(pointsB);
        bottomRing.logic.setEnableConstantVelocity(true);
        bottomRing.logic.setConstantVelocity(1);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).whileTrue(topRing);
        ArrayList<XbotSwervePoint> pointsX = new ArrayList<>();
        pointsX.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(8.1, 7.4), Rotation2d.fromDegrees(0), 10));
        topRing.logic.setKeyPoints(pointsY);
        topRing.logic.setEnableConstantVelocity(true);
        topRing.logic.setConstantVelocity(1);


        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(middleRing);
        ArrayList<XbotSwervePoint> pointsLB = new ArrayList<>();
        pointsX.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(8, 4), Rotation2d.fromDegrees(0), 10));
        middleRing.logic.setKeyPoints(pointsLB);
        middleRing.logic.setEnableConstantVelocity(true);
        middleRing.logic.setConstantVelocity(1);
    }
}
