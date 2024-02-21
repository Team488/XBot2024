package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.auto.DriveCoupleFeetCommand;
import competition.auto.Fast4NoteCloseCommand;
import competition.auto.Fast4NoteFarCommand;
import competition.auto.FourNoteAutoCommand;
import competition.auto.MarkerTestAutoCommand;
import competition.auto.MidNoteCommand;
import competition.auto.UpdateHolonomicCommand;
import competition.subsystems.arm.commands.ReconcileArmAlignmentCommand;
import competition.subsystems.oracle.SwerveAccordingToOracleCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
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
            SwerveSimpleTrajectoryCommand swerveTest,
            SwerveSimpleTrajectoryCommand avoidColumnTest,
            SwerveAccordingToOracleCommand oracleSwerve,
            ManualRobotKnowledgeSubsystem knowledgeSubsystem,
            DynamicOracle oracle,
            ReconcileArmAlignmentCommand slightLeftArmForward,
            ReconcileArmAlignmentCommand slightLeftArmBackward,
            DriveCoupleFeetCommand driveCoupleFeetCommand,
            FourNoteAutoCommand fourNoteAutoCommand,
            UpdateHolonomicCommand updateHolonomicCommand,
            MidNoteCommand midNoteCommand,
            Fast4NoteFarCommand fast4NoteFarCommand,
            Fast4NoteCloseCommand fast4NoteCloseCommand,
            MarkerTestAutoCommand markerTestAutoCommand)
    {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).onTrue(resetHeading);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).onTrue(pose.createCopyFusedOdometryToWheelsOdometryCommand());
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(pose.createSetPositionCommand(
                new Pose2d(2.6, 5.65, Rotation2d.fromDegrees(0))));


        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).onTrue(driveCoupleFeetCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).onTrue(fourNoteAutoCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(updateHolonomicCommand);
        midNoteCommand.includeOnSmartDashboard();
        fast4NoteFarCommand.includeOnSmartDashboard();
        fast4NoteCloseCommand.includeOnSmartDashboard();
        markerTestAutoCommand.includeOnSmartDashboard();
    }
}