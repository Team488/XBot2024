package competition.operator_interface;

import competition.commandgroups.PrepareToFireAtSpeakerFromFarAmpCommand;
import competition.subsystems.oracle.ListenToOracleCommandGroup;
import competition.auto_programs.SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter;
import competition.auto_programs.SubwooferShotFromBotShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromMidShootThenShootNearestThree;
import competition.auto_programs.SubwooferShotFromTopShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter;
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
import competition.subsystems.drive.commands.DriveToAmpCommand;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import competition.subsystems.schoocher.commands.IntakeScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.commandgroups.PrepareToFireAtSpeakerFromPodiumCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import competition.subsystems.shooter.commands.WarmUpShooterRPMCommand;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.autonomous.SetAutonomousCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

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
            WarmUpShooterCommand shooterWarmUpTypical,
            WarmUpShooterCommand shooterWarmUpAmp,
            FireCollectorCommand fireCollectorCommand
    ) {
        // Scooch
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(scoocherIntakeProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(scoocherEject);

        // Collect
        var rumbleModeFalse = new InstantCommand(() -> oi.operatorFundamentalsGamepad.getRumbleManager().stopGamepadRumble());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightTrigger).whileTrue(collectorIntake).onFalse(rumbleModeFalse);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectorEject);

        // Fire
        shooterWarmUpTypical.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        shooterWarmUpAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.A).whileTrue(shooterWarmUpTypical);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Back).whileTrue(shooterWarmUpAmp);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.B).whileTrue(fireCollectorCommand);
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
            DriveToCentralSubwooferCommand driveToCentralSubwooferCommand,
            DriveToAmpCommand driveToAmpCommand
            )
    {
        // Rotation calibration routine
        resetHeading.setHeadingToApply(() -> PoseSubsystem.convertBlueToRedIfNeeded(Rotation2d.fromDegrees(180)).getDegrees());

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(resetHeading);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).whileTrue(alignToNoteCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).whileTrue(driveToCentralSubwooferCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).whileTrue(driveToAmpCommand);
    }

    @Inject
    public void setupOracleCommands(OperatorInterface oi,
                                    ListenToOracleCommandGroup listenToOracleCommandGroup) {

        oi.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(listenToOracleCommandGroup);
    }

    @Inject
    public void setupArmFineAdjustmentCommands(
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
            PrepareToFireAtSpeakerFromPodiumCommand prepareToFireAtSpeakerFromPodium,
            PrepareToFireAtSpeakerFromFarAmpCommand prepareToFireAtSpeakerFromFarAmp,
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
        warmUpShooterSubwoofer.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);

        var warmUpShooterToFireInAmp = warmUpShooterCommandProvider.get();
        warmUpShooterToFireInAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);

        // Combine into useful actions
        // Note manipulation:
        var collectNote = intakeCollector.alongWith(armToCollection);
        var scoochNote = intakeScoocher.alongWith(armToScooch);

        // Preparing to score:
        var prepareToFireAtSubwoofer = warmUpShooterSubwoofer.alongWith(armToSubwoofer);
        var prepareToFireAtAmp = warmUpShooterToFireInAmp.alongWith(armToAmp);
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

        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.B).whileTrue(prepareToFireAtSpeakerFromPodium);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightBumper).whileTrue(prepareToFireAtSpeakerFromFarAmp);
    }
    
    @Inject
    public void setupForceRobotToPositionCommands(PoseSubsystem pose,
                                                  OperatorInterface oi) {
        var teleportRobotToSubwooferTop = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation)).ignoringDisable(true);
        oi.neoTrellis.getifAvailable(17).onTrue(teleportRobotToSubwooferTop);

        var teleportRobotToSubwooferMid = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferCentralScoringLocation)).ignoringDisable(true);
        oi.neoTrellis.getifAvailable(18).onTrue(teleportRobotToSubwooferMid);

        var teleportRobotToSubwooferBottom = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation)).ignoringDisable(true);
        oi.neoTrellis.getifAvailable(19).onTrue(teleportRobotToSubwooferBottom);
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
