package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.FireCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.SwerveAccordingToOracleCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import competition.subsystems.schoocher.commands.IntakeScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;
import xbot.common.trajectory.LowResField;
import xbot.common.trajectory.XbotSwervePoint;

import java.util.ArrayList;

/**
 * Maps operator interface buttons to commands
 */
@Singleton
public class OperatorCommandMap {

    @Inject
    public OperatorCommandMap() {}


    @Inject
    public void setupFundamentalCommands(
            OperatorInterface oi,
            IntakeScoocherCommand scoocherIntake,
            EjectScoocherCommand scoocherEject,
            IntakeCollectorCommand collectorIntake,
            EjectCollectorCommand collectorEject,
            WarmUpShooterCommand shooterWarmUp,
            FireCollectorCommand fireCollectorCommand,
            SetArmAngleCommand armAngle
    ) {
        // Scooch
        oi.operatorGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(scoocherIntake);
        oi.operatorGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(scoocherEject);

        // Collect
        oi.operatorGamepad.getXboxButton(XboxButton.RightTrigger).whileTrue(collectorIntake);
        oi.operatorGamepad.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectorEject);

        // Fire
        shooterWarmUp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.NEARSHOT);
        oi.operatorGamepad.getXboxButton(XboxButton.X).whileTrue(shooterWarmUp);
        oi.operatorGamepad.getXboxButton(XboxButton.A).whileTrue(fireCollectorCommand);

        // Arms are taken care of via their maintainer & human overrides.
        armAngle.setArmPosition(ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE);
        var scoochNote = scoocherIntake.alongWith(armAngle);
        // TODO: bind scoochNote action to a button in operatorGamepad
    }
    
    // Example for setting up a command to fire when a button is pressed:
    @Inject
    public void setupMobilityComands(
            PoseSubsystem pose,
            OperatorInterface operatorInterface,
            Provider<SwerveSimpleTrajectoryCommand> swerveCommandProvider,
            SetRobotHeadingCommand resetHeading,
            DynamicOracle oracle,
            DriveSubsystem drive
            )
    {
        double typicalVelocity = 2.5;
        // Manipulate heading and position for easy testing
        resetHeading.setHeadingToApply(0);
        var teleportRobot = pose.createSetPositionCommand(new Pose2d(2.6, 5.65, Rotation2d.fromDegrees(0)));

        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).onTrue(resetHeading);
        LowResField fieldWithObstacles = oracle.getFieldWithObstacles();

        var noviceMode = new InstantCommand(() -> drive.setNoviceMode(true));
        var expertMode = new InstantCommand(() -> drive.setNoviceMode(false));

        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftStick).onTrue(noviceMode);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightStick).onTrue(expertMode);

        // Where are some cool places we may want to go..
        // 1) Where there are Notes!
        var goToTopSpike = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.SpikeTop, typicalVelocity, fieldWithObstacles, true);
        var goToMiddleSpike = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.SpikeMiddle, typicalVelocity, fieldWithObstacles, true);
        var goToBottomSpike = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.SpikeBottom, typicalVelocity, fieldWithObstacles, true);

        var goToCenterLine1 = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.CenterLine1, typicalVelocity, fieldWithObstacles, true);
        var goToCenterLine2 = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.CenterLine2, typicalVelocity, fieldWithObstacles, true);
        var goToCenterLine3 = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.CenterLine3, typicalVelocity, fieldWithObstacles, true);
        var goToCenterLine4 = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.CenterLine4, typicalVelocity, fieldWithObstacles, true);
        var goToCenterLine5 = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.CenterLine5, typicalVelocity, fieldWithObstacles, true);

        // 2) Or to go score!
        var goToAmp = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.AmpScoringLocation, typicalVelocity, fieldWithObstacles);
        var goToSpeaker = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.SubwooferCentralScoringLocation, typicalVelocity, fieldWithObstacles);

        // 3) Or pick up a new note from the source!
        var goToNoteSource = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.NearbySource, typicalVelocity, fieldWithObstacles, true);

        // Bind these to buttons on the neotrellis.
        operatorInterface.neoTrellis.getifAvailable(10).whileTrue(goToTopSpike);
        operatorInterface.neoTrellis.getifAvailable(11).whileTrue(goToMiddleSpike);
        operatorInterface.neoTrellis.getifAvailable(12).whileTrue(goToBottomSpike);

        operatorInterface.neoTrellis.getifAvailable(2).whileTrue(goToCenterLine1);
        operatorInterface.neoTrellis.getifAvailable(3).whileTrue(goToCenterLine2);
        operatorInterface.neoTrellis.getifAvailable(4).whileTrue(goToCenterLine3);
        operatorInterface.neoTrellis.getifAvailable(5).whileTrue(goToCenterLine4);
        operatorInterface.neoTrellis.getifAvailable(6).whileTrue(goToCenterLine5);

        operatorInterface.neoTrellis.getifAvailable(9).whileTrue(goToAmp);
        operatorInterface.neoTrellis.getifAvailable(26).whileTrue(goToSpeaker);
        operatorInterface.neoTrellis.getifAvailable(14).whileTrue(goToNoteSource);

    }

    @Inject
    public void scoringCommands(
            SetArmAngleCommand armAngle,
            WarmUpShooterCommand shooter
            )
    {
        // Prepare to score in Amp
        armAngle.setArmPosition(ArmSubsystem.UsefulArmPosition.FIRING_IN_AMP);
        shooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.AMP_SHOT);
        var prepareToScoreInAmp = armAngle.alongWith(shooter);
        // TODO: Bind command to a button in operatorGamepad
    }

    @Inject
    public void setupOracleCommands(OperatorInterface oi,
                                    SwerveAccordingToOracleCommand oracleSwerve,
                                    ManualRobotKnowledgeSubsystem knowledgeSubsystem,
                                    DynamicOracle oracle) {
        oracleSwerve.logic.setEnableConstantVelocity(true);
        oracleSwerve.logic.setConstantVelocity(2.8);
        oracleSwerve.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());

        oi.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(oracleSwerve);
        oi.driverGamepad.getXboxButton(XboxButton.LeftBumper)
                .whileTrue(knowledgeSubsystem.createSetNoteCollectedCommand());
        oi.driverGamepad.getXboxButton(XboxButton.RightBumper)
                .whileTrue(knowledgeSubsystem.createSetNoteShotCommand());
    }

    private SwerveSimpleTrajectoryCommand createAndConfigureTypicalSwerveCommand(
            SwerveSimpleTrajectoryCommand command, Pose2d target, double targetVelocity, LowResField fieldWithObstacles) {

        return createAndConfigureTypicalSwerveCommand(command, target, targetVelocity,
                fieldWithObstacles, false);
    }

    private SwerveSimpleTrajectoryCommand createAndConfigureTypicalSwerveCommand(
            SwerveSimpleTrajectoryCommand command, Pose2d target, double targetVelocity, LowResField fieldWithObstacles,
            boolean aimAtGoalDuringFinalLeg) {

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(new XbotSwervePoint(
                target.getTranslation(), target.getRotation(), 10));
        command.logic.setEnableConstantVelocity(true);
        command.logic.setConstantVelocity(targetVelocity);
        command.logic.setFieldWithObstacles(fieldWithObstacles);
        command.logic.setAimAtGoalDuringFinalLeg(aimAtGoalDuringFinalLeg);

        command.logic.setKeyPoints(points);

        return command;
    }
}
