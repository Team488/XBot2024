package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.auto.Far4NoteCommandGroup;
import competition.auto.PodiumMidCommandGroup;

import competition.auto_programs.DistanceShotFromMidShootThenShootMiddleTopThenTopCenter;
import competition.auto_programs.DistanceShotFromMidShootThenShootNearestThree;

import competition.auto_programs.FromMidShootCollectShoot;
import competition.subsystems.oracle.ListenToOracleCommandGroup;
import competition.auto_programs.SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter;
import competition.auto_programs.SubwooferShotFromBotShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromMidShootThenShootNearestThree;
import competition.auto_programs.SubwooferShotFromTopShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter;
import competition.commandgroups.PrepareToFireAtAmpCommandGroup;
import competition.commandgroups.PrepareToFireAtSpeakerCommandGroup;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.CalibrateArmsManuallyCommand;
import competition.subsystems.arm.commands.ContinuouslyPointArmAtSpeakerCommand;
import competition.subsystems.arm.commands.DisengageBrakeCommand;
import competition.subsystems.arm.commands.EngageBrakeCommand;
import competition.subsystems.arm.commands.ManualHangingModeCommand;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.FireCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.AlignToNoteCommand;

import competition.subsystems.oracle.SuperstructureAccordingToOracleCommand;
import competition.subsystems.oracle.SwerveAccordingToOracleCommand;
import competition.subsystems.drive.commands.DriveToAmpCommand;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToMidSpikeScoringLocationCommand;
import competition.subsystems.drive.commands.PointAtSpeakerCommand;

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
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.autonomous.SetAutonomousCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;
import xbot.common.trajectory.LowResField;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
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
            DriveToCentralSubwooferCommand driveToCentralSubwooferCommand,
            DriveToAmpCommand driveToAmpCommand
            )
    {
        double typicalVelocity = 2.5;
        // Manipulate heading and position for easy testing
        resetHeading.setHeadingToApply(() -> PoseSubsystem.convertBlueToRedIfNeeded(Rotation2d.fromDegrees(180)).getDegrees());

        var teleportRobotToSubwooferTop = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation)).ignoringDisable(true);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).onTrue(teleportRobotToSubwooferTop);
        var teleportRobotToSubwooferMid = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferCentralScoringLocation)).ignoringDisable(true);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).onTrue(teleportRobotToSubwooferMid);
        var teleportRobotToSubwooferBottom = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation)).ignoringDisable(true);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).onTrue(teleportRobotToSubwooferBottom);


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
                swerveCommandProvider.get(), PoseSubsystem.BlueSubwooferCentralScoringLocation, typicalVelocity, fieldWithObstacles);

        // 3) Or pick up a new note from the source!
        var goToNoteSource = createAndConfigureTypicalSwerveCommand(
                swerveCommandProvider.get(), PoseSubsystem.NearbySource, typicalVelocity, fieldWithObstacles, true);

        // Bind these to buttons on the neotrellis.
        operatorInterface.neoTrellis.getifAvailable(9).whileTrue(goToAmp);
        operatorInterface.neoTrellis.getifAvailable(14).whileTrue(goToNoteSource);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).whileTrue(driveToCentralSubwooferCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).whileTrue(driveToAmpCommand);
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
                                    ListenToOracleCommandGroup listenToOracleCommandGroup,
                                    ManualRobotKnowledgeSubsystem knowledgeSubsystem,
                                    DynamicOracle oracle) {

        oi.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(listenToOracleCommandGroup);
        oi.driverGamepad.getPovIfAvailable(0).onTrue(new InstantCommand(() -> oracle.resetNoteMap()));
        oi.driverGamepad.getPovIfAvailable(270).onTrue(new InstantCommand(() -> oracle.freezeConfigurationForAutonomous()));

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
            FireWhenReadyCommand fireWhenReady,
            ManualHangingModeCommand manualHangingModeCommand
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
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.B).whileTrue(manualHangingModeCommand);

        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightTrigger).whileTrue(fireWhenReady.repeatedly());
    }

    @Inject
    public void setupAutonomousForTesting(OperatorInterface oi,
                                          FromMidShootCollectShoot fromMidShootCollectShoot,
                                          SubwooferShotFromMidShootThenShootNearestThree subwooferFour,
                                          DistanceShotFromMidShootThenShootNearestThree distanceFour,
                                          PointAtSpeakerCommand pointAtSpeakerCommand,
                                          DriveToMidSpikeScoringLocationCommand driveToMidSpikeScoringLocationCommand,
                                          DistanceShotFromMidShootThenShootMiddleTopThenTopCenter distanceShotPreTopTwoSpikesTopCenter,
                                          DriveToListOfPointsCommand driveToListOfPointsCommand,
                                          SubwooferShotFromBotShootThenShootSpikes subwooferShotFromBotShootThenShootSpikes,
                                          SubwooferShotFromTopShootThenShootSpikes subwooferShotFromTopShootThenShootSpikes,
                                          SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter subShotFromBotBotSpikeBotCenter,
                                          SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter subShotFromTopTopSpikeTopCenter) {
        oi.operatorGamepadAdvanced.getPovIfAvailable(0).whileTrue(fromMidShootCollectShoot);
        oi.operatorGamepadAdvanced.getPovIfAvailable(90).whileTrue(distanceFour);
        oi.operatorGamepadAdvanced.getPovIfAvailable(180).whileTrue(distanceShotPreTopTwoSpikesTopCenter);
        oi.operatorGamepadAdvanced.getPovIfAvailable(270).whileTrue(subwooferFour);

        oi.operatorGamepadAdvanced.getPovIfAvailable(45).whileTrue(subwooferShotFromBotShootThenShootSpikes);
        oi.operatorGamepadAdvanced.getPovIfAvailable(135).whileTrue(subwooferShotFromTopShootThenShootSpikes);
        oi.operatorGamepadAdvanced.getPovIfAvailable(225).whileTrue(subShotFromBotBotSpikeBotCenter);
        oi.operatorGamepadAdvanced.getPovIfAvailable(315).whileTrue(subShotFromTopTopSpikeTopCenter);

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

    @Inject
    public void setupAutonomousCommandSelection(OperatorInterface oi,
                                                Provider<SetAutonomousCommand> setAutonomousCommandProvider,
                                                ListenToOracleCommandGroup listenToOracleCommandGroup,
                                                SubwooferShotFromMidShootThenShootNearestThree midThenThree,
                                                SubwooferShotFromTopShootThenShootSpikes topThenThree,
                                                SubwooferShotFromBotShootThenShootSpikes botThenThree,
                                                SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter topThenTopSpikeTopCenter,
                                                SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter botThenBotSpikeBotCenter) {
        var setOracleAuto = setAutonomousCommandProvider.get();
        setOracleAuto.setAutoCommand(listenToOracleCommandGroup);
        oi.neoTrellis.getifAvailable(31).onTrue(setOracleAuto);

        var setMidThenThree = setAutonomousCommandProvider.get();
        setMidThenThree.setAutoCommand(midThenThree);
        oi.neoTrellis.getifAvailable(23).onTrue(setMidThenThree);

        var setTopThenThree = setAutonomousCommandProvider.get();
        setTopThenThree.setAutoCommand(topThenThree);
        oi.neoTrellis.getifAvailable(15).onTrue(setTopThenThree);

        var setBotThenThree = setAutonomousCommandProvider.get();
        setBotThenThree.setAutoCommand(botThenThree);
        oi.neoTrellis.getifAvailable(7).onTrue(setBotThenThree);

        var setTopThenTopSpikeTopCenter = setAutonomousCommandProvider.get();
        setTopThenTopSpikeTopCenter.setAutoCommand(topThenTopSpikeTopCenter);
        oi.neoTrellis.getifAvailable(32).onTrue(setTopThenTopSpikeTopCenter);

        var setBotThenBotSpikeBotCenter = setAutonomousCommandProvider.get();
        setBotThenBotSpikeBotCenter.setAutoCommand(botThenBotSpikeBotCenter);
        oi.neoTrellis.getifAvailable(24).onTrue(setBotThenBotSpikeBotCenter);
    }
}

