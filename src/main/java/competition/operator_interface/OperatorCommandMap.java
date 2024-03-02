package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.auto.Far4NoteCommandGroup;
import competition.auto.PodiumMidCommandGroup;
import competition.auto_programs.FromMidShootCollectShoot;
import competition.commandgroups.PrepareToFireAtAmpCommandGroup;
import competition.commandgroups.PrepareToFireAtSpeakerCommandGroup;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.CalibrateArmsManuallyCommand;
import competition.subsystems.arm.commands.ContinuouslyPointArmAtSpeakerCommand;
import competition.subsystems.arm.commands.DisengageBrakeCommand;
import competition.subsystems.arm.commands.EngageBrakeCommand;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.FireCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.AlignToNoteCommand;
import competition.subsystems.oracle.SuperstructureAccordingToOracleCommand;
import competition.subsystems.oracle.SwerveAccordingToOracleCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import competition.subsystems.schoocher.commands.IntakeScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import competition.subsystems.shooter.commands.WarmUpShooterRPMCommand;
import edu.wpi.first.math.geometry.Pose2d;
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
            Provider<IntakeScoocherCommand> scoocherIntakeProvider,
            EjectScoocherCommand scoocherEject,
            IntakeCollectorCommand collectorIntake,
            EjectCollectorCommand collectorEject,
            SetArmAngleCommand armAngle,
            PrepareToFireAtSpeakerCommandGroup prepareToFireAtSpeakerCommandGroup,
            WarmUpShooterCommand shooterWarmUpSafe,
            WarmUpShooterCommand shooterWarmUpNear,
            WarmUpShooterCommand shooterWarmUpFar,
            WarmUpShooterCommand shooterWarmUpAmp,
            FireCollectorCommand fireCollectorCommand,
            WarmUpShooterRPMCommand warmUpShooterDifferentialRPM,
            EngageBrakeCommand engageBrake,
            DisengageBrakeCommand disengageBrake
    ) {
        // Scooch
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(scoocherIntakeProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(scoocherEject);

        // Collect
        var rumbleModeFalse = new InstantCommand(() -> oi.operatorFundamentalsGamepad.getRumbleManager().stopGamepadRumble());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightTrigger).whileTrue(collectorIntake).onFalse(rumbleModeFalse);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectorEject);

        // Fire
        shooterWarmUpSafe.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        shooterWarmUpNear.setTargetRpm(ShooterWheelSubsystem.TargetRPM.NEARSHOT);
        shooterWarmUpFar.setTargetRpm(ShooterWheelSubsystem.TargetRPM.DISTANCESHOT);
        shooterWarmUpAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.AMP_SHOT);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.A).whileTrue(shooterWarmUpSafe);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.X).whileTrue(shooterWarmUpNear);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Y).whileTrue(shooterWarmUpFar);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Back).whileTrue(shooterWarmUpAmp);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.B).whileTrue(fireCollectorCommand);

        // Arms are taken care of via their maintainer & han overrides.
        armAngle.setArmPosition(ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE);
        var scoochNote = scoocherIntakeProvider.get();
        scoochNote.alongWith(armAngle);
        // TODO: bind scoochNote action to a button in operatorGamepad

        warmUpShooterDifferentialRPM.setTargetRpm(new ShooterWheelTargetSpeeds(1000, 2000));
        //oi.operatorFundamentalsGamepad.getPovIfAvailable(0).whileTrue(warmUpShooterDifferentialRPM);

        engageBrake.setBrakeMode(true);
        disengageBrake.setBrakeMode(false);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightJoystickYAxisPositive).onTrue(engageBrake);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightJoystickYAxisNegative).onTrue(disengageBrake);
    }

    // Example for setting up a command to fire when a button is pressed:
    @Inject
    public void setupMobilityComands(
            PoseSubsystem pose,
            OperatorInterface operatorInterface,
            Provider<SwerveSimpleTrajectoryCommand> swerveCommandProvider,
            SetRobotHeadingCommand resetHeading,
            DynamicOracle oracle,
            DriveSubsystem drive,
            FireWhenReadyCommand fireWhenReady,
            FireCollectorCommand fireCollector,
            AlignToNoteCommand alignToNoteCommand,
            Far4NoteCommandGroup far4NoteCommandGroup,
            PodiumMidCommandGroup podiumMidCommandGroup)

    {
        double typicalVelocity = 2.5;
        // Manipulate heading and position for easy testing
        resetHeading.setHeadingToApply(180);
        var teleportRobot = pose.createSetPositionCommand(PoseSubsystem.SubwooferCentralScoringLocation);
        operatorInterface.driverGamepad.getPovIfAvailable(180).onTrue(teleportRobot);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(resetHeading);
        LowResField fieldWithObstacles = oracle.getFieldWithObstacles();

        var noviceMode = new InstantCommand(() -> drive.setNoviceMode(true));
        var expertMode = new InstantCommand(() -> drive.setNoviceMode(false));

        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftStick).onTrue(noviceMode);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightStick).onTrue(expertMode);


//        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightBumper).onTrue(fireWhenReady);

//        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftBumper).onTrue();
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).onTrue(far4NoteCommandGroup);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).onTrue(podiumMidCommandGroup);


        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).whileTrue(alignToNoteCommand);




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
            PrepareToFireAtAmpCommandGroup prepareToFireAtAmpCommand,
            PrepareToFireAtSpeakerCommandGroup prepareToFireAtSpeakerCommand
    )
    {
        // TODO: Bind prepareToFireAtAmpCommand to a button in operatorGamepad
        // TODO: Bind prepareToFireAtSpeakerCommand to a button in operatorGamepad
    }

    @Inject
    public void setupOracleCommands(OperatorInterface oi,
                                    SwerveAccordingToOracleCommand driveAccoringToOracle,
                                    SuperstructureAccordingToOracleCommand superstructureAccordingToOracle,
                                    ManualRobotKnowledgeSubsystem knowledgeSubsystem,
                                    DynamicOracle oracle) {
        driveAccoringToOracle.logic.setEnableConstantVelocity(true);
        driveAccoringToOracle.logic.setConstantVelocity(2.8);
        driveAccoringToOracle.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());

        oi.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(driveAccoringToOracle.alongWith(superstructureAccordingToOracle));
        oi.driverGamepad.getPovIfAvailable(0).onTrue(new InstantCommand(() -> oracle.resetNoteMap()));

    }

    @Inject
    public void setupArmPIDCommands(
            OperatorInterface oi,
            Provider<SetArmExtensionCommand> commandProvider,
            CalibrateArmsManuallyCommand calibrateArmsManuallyCommand) {
        var homeArm = commandProvider.get();
        homeArm.setTargetExtension(0);

        var highArm = commandProvider.get();
        highArm.setTargetExtension(150);

        var increaseArmLarge = commandProvider.get();
        increaseArmLarge.setTargetExtension(20);
        increaseArmLarge.setRelative(true);

        var increaseArmSmall = commandProvider.get();
        increaseArmSmall.setTargetExtension(2);
        increaseArmSmall.setRelative(true);

        var decreaseArmLarge = commandProvider.get();
        decreaseArmLarge.setTargetExtension(-20);
        decreaseArmLarge.setRelative(true);

        var decreaseArmSmall = commandProvider.get();
        decreaseArmSmall.setTargetExtension(-2);
        decreaseArmSmall.setRelative(true);

        oi.operatorFundamentalsGamepad.getPovIfAvailable(0).whileTrue(increaseArmLarge);
        oi.operatorFundamentalsGamepad.getPovIfAvailable(180).whileTrue(decreaseArmLarge);
        oi.operatorFundamentalsGamepad.getPovIfAvailable(90).whileTrue(increaseArmSmall);
        oi.operatorFundamentalsGamepad.getPovIfAvailable(270).whileTrue(decreaseArmSmall);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Start).onTrue(calibrateArmsManuallyCommand);
    }

    @Inject
    public void setupAdvancedOperatorCommands(
            OperatorInterface oi,
            ArmSubsystem arm,
            Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
            IntakeCollectorCommand intakeCollector,
            EjectCollectorCommand ejectCollector,
            IntakeScoocherCommand intakeScoocher,
            EjectScoocherCommand ejectScoocher,
            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
            ContinuouslyPointArmAtSpeakerCommand continuouslyPointArmAtSpeaker,
            ContinuouslyWarmUpForSpeakerCommand continuouslyWarmUpForSpeaker,
            FireWhenReadyCommand fireWhenReady
    ) {
        //Useful arm positions
        var armToCollection = setArmExtensionCommandProvider.get();
        armToCollection.setTargetExtension(arm.getUsefulArmPositionExtensionInMm(
                ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND));

        var armToScooch = setArmExtensionCommandProvider.get();
        armToScooch.setTargetExtension(arm.getUsefulArmPositionExtensionInMm(
                ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE));

        var armToSubwoofer = setArmExtensionCommandProvider.get();
        armToSubwoofer.setTargetExtension(arm.getUsefulArmPositionExtensionInMm(
                ArmSubsystem.UsefulArmPosition.FIRING_FROM_SUBWOOFER));

        var armToAmp = setArmExtensionCommandProvider.get();
        armToAmp.setTargetExtension(arm.getUsefulArmPositionExtensionInMm(
                ArmSubsystem.UsefulArmPosition.FIRING_FROM_AMP));

        // Useful wheel speeds
        var warmUpShooterSubwoofer = warmUpShooterCommandProvider.get();
        warmUpShooterSubwoofer.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);

        var warmUpShooterAmp = warmUpShooterCommandProvider.get();
        warmUpShooterAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.AMP_SHOT);

        // Combine into useful actions
        // Note manipulation:
        var collectNote = intakeCollector.alongWith(armToCollection);
        var scoochNote = intakeScoocher.alongWith(armToScooch);

        // Preparing to score:
        var prepareToFireAtSubwoofer = warmUpShooterSubwoofer.alongWith(armToSubwoofer);
        var prepareToFireAtAmp = warmUpShooterAmp.alongWith(armToAmp);
        var continuouslyPrepareToFireAtSpeaker =
                continuouslyWarmUpForSpeaker.alongWith(continuouslyPointArmAtSpeaker);

        // Bind to buttons
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectNote);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Back).whileTrue(ejectCollector);

        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.LeftBumper).whileTrue(scoochNote);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Start).whileTrue(ejectScoocher);

        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.X).whileTrue(prepareToFireAtSubwoofer);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Y).whileTrue(prepareToFireAtAmp);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.A).whileTrue(continuouslyPrepareToFireAtSpeaker);

        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightTrigger).whileTrue(fireWhenReady.repeatedly());
    }

    @Inject
    public void setupAutonomousForTesting(OperatorInterface oi,
                                          FromMidShootCollectShoot fromMidShootCollectShoot) {
        oi.operatorGamepadAdvanced.getPovIfAvailable(0).whileTrue(fromMidShootCollectShoot);
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