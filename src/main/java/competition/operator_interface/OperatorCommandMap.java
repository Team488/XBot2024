package competition.operator_interface;

import competition.auto_programs.SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter;
import competition.auto_programs.SubwooferShotFromBotShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromMidShootThenShootNearestThree;
import competition.auto_programs.SubwooferShotFromTopShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromTopShootThenShootTopSpikeThenShootTwoCenter;
import competition.commandgroups.PrepareToFireAtSpeakerFromPodiumCommand;
import competition.commandgroups.PrepareToFireNearestGoodScoringPositionCommand;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.CalibrateArmsManuallyCommand;
import competition.subsystems.arm.commands.ContinuouslyPointArmAtSpeakerCommand;
import competition.subsystems.arm.commands.ForceEngageBrakeCommand;
import competition.subsystems.arm.commands.LimitArmToUnderStage;
import competition.subsystems.arm.commands.ManualHangingModeCommand;
import competition.subsystems.arm.commands.PrepareForHangingCommand;
import competition.subsystems.arm.commands.RemoveForcedBrakingCommand;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.FireCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToAmpCommand;
import competition.subsystems.drive.commands.DriveToNearestGoodScoringPositionCommand;
import competition.subsystems.drive.commands.LineUpForHangingCommand;
import competition.subsystems.drive.commands.PointAtNoteCommand;
import competition.subsystems.flipper.commands.ToggleFlipperCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.ListenToOracleCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import competition.subsystems.schoocher.commands.IntakeScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.controls.sensors.XXboxController.XboxButton;
import xbot.common.subsystems.autonomous.SetAutonomousCommand;
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


    
    // Example for setting up a command to fire when a button is pressed:
    @Inject
    public void setupDriverCommands(
            OperatorInterface operatorInterface,
            SetRobotHeadingCommand resetHeading,
            DriveSubsystem drive,
            PointAtNoteCommand alignToNoteCommand,
            LineUpForHangingCommand lineUpForHangingCommand,
            DriveToAmpCommand driveToAmpCommand,
            ListenToOracleCommandGroup listenToOracleCommandGroup,
            DriveToNearestGoodScoringPositionCommand driveToNearestGoodScoringPositionCommand,
            LimitArmToUnderStage limitArmToUnderStageCommand)
    {
        // Rotation calibration routine
        resetHeading.setHeadingToApply(() -> PoseSubsystem.convertBlueToRedIfNeeded(Rotation2d.fromDegrees(180)).getDegrees());

        var pointAtSpeaker = drive.createSetSpecialPointAtPositionTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_TARGET_FORWARD));

        var pointAtSource = drive.createSetSpecialHeadingTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.FaceCollectorToBlueSource));

        var cancelSpecialPointAtPosition = drive.createClearAllSpecialTargetsCommand();

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(listenToOracleCommandGroup);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(resetHeading);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(driveToAmpCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(driveToNearestGoodScoringPositionCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).whileTrue(limitArmToUnderStageCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).whileTrue(alignToNoteCommand);

        operatorInterface.driverGamepad.getXboxButton(XboxButton.B)
                .onTrue(pointAtSpeaker)
                .onFalse(cancelSpecialPointAtPosition);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y)
                .onTrue(pointAtSource)
                .onFalse(cancelSpecialPointAtPosition);

        operatorInterface.driverGamepad.getPovIfAvailable(90).whileTrue(lineUpForHangingCommand);

    }


    @Inject
    public void setupAdvancedOperatorCommands(
            OperatorInterface oi,
            Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
            Provider<IntakeCollectorCommand> intakeCollectorProvider,
            EjectCollectorCommand ejectCollector,
            IntakeScoocherCommand intakeScoocher,
            EjectScoocherCommand ejectScoocher,
            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
            ContinuouslyPointArmAtSpeakerCommand continuouslyPointArmAtSpeaker,
            ContinuouslyWarmUpForSpeakerCommand continuouslyWarmUpForSpeaker,
            FireWhenReadyCommand fireWhenReady,
            PrepareToFireAtSpeakerFromPodiumCommand prepareToFireAtSpeakerFromPodium,
            PrepareToFireNearestGoodScoringPositionCommand prepareToFireNearestGoodScoringPositionCommand,
            ManualHangingModeCommand manualHangingModeCommand,
            ForceEngageBrakeCommand forceEngageBrakeCommand,
            RemoveForcedBrakingCommand removeForcedBrakingCommand,
            PrepareForHangingCommand prepareForHangingCommand,
            ToggleFlipperCommand toggleFlipperCommand
    ) {
        //Useful arm positions
        var armToCollection = setArmExtensionCommandProvider.get();
        armToCollection.setTargetExtension(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        var armToScooch = setArmExtensionCommandProvider.get();
        armToScooch.setTargetExtension(ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE);

        var armToSubwoofer = setArmExtensionCommandProvider.get();
        armToSubwoofer.setTargetExtension(ArmSubsystem.UsefulArmPosition.FIRING_FROM_SUBWOOFER);

        var armToAmp = setArmExtensionCommandProvider.get();
        armToAmp.setTargetExtension(ArmSubsystem.UsefulArmPosition.FIRING_FROM_AMP);

        var armToSource = setArmExtensionCommandProvider.get();
        armToSource.setTargetExtension(ArmSubsystem.UsefulArmPosition.COLLECT_DIRECTLY_FROM_SOURCE);

        // Useful wheel speeds
        var warmUpShooterSubwoofer = warmUpShooterCommandProvider.get();
        warmUpShooterSubwoofer.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);

        var warmUpShooterToFireInAmp = warmUpShooterCommandProvider.get();
        warmUpShooterToFireInAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);

        // Combine into useful actions
        // Note manipulation:
        var collectNoteFromGround = intakeCollectorProvider.get().alongWith(armToCollection);
        var collectNoteFromSource = intakeCollectorProvider.get().alongWith(armToSource);
        var scoochNote = intakeScoocher.alongWith(armToScooch);

        // Preparing to score:
        var prepareToFireAtSubwoofer = warmUpShooterSubwoofer.alongWith(armToSubwoofer);
        var prepareToFireAtAmp = warmUpShooterToFireInAmp.alongWith(armToAmp);
        var continuouslyPrepareToFireAtSpeaker =
                continuouslyWarmUpForSpeaker.alongWith(continuouslyPointArmAtSpeaker);

        // Bind to buttons
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectNoteFromGround);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightTrigger).whileTrue(fireWhenReady.repeatedly());
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.LeftBumper).whileTrue(scoochNote);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightBumper).whileTrue(continuouslyPrepareToFireAtSpeaker);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Back).whileTrue(ejectCollector);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Start).whileTrue(prepareForHangingCommand);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.A).whileTrue(prepareToFireAtAmp);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.B).whileTrue(prepareToFireNearestGoodScoringPositionCommand);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.X).whileTrue(prepareToFireAtSpeakerFromPodium);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Y).whileTrue(prepareToFireAtSubwoofer);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightJoystickYAxisPositive).onTrue(forceEngageBrakeCommand);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightJoystickYAxisNegative).onTrue(removeForcedBrakingCommand);
        oi.operatorGamepadAdvanced.getPovIfAvailable(0).whileTrue(collectNoteFromSource);
        oi.operatorGamepadAdvanced.getPovIfAvailable(180).whileTrue(manualHangingModeCommand);
        oi.operatorGamepadAdvanced.getPovIfAvailable(90).whileTrue(toggleFlipperCommand);
    }

    @Inject
    public void setupFundamentalCommands(
            OperatorInterface oi,
            Provider<IntakeScoocherCommand> scoocherIntakeProvider,
            EjectScoocherCommand scoocherEject,
            IntakeCollectorCommand collectorIntake,
            EjectCollectorCommand collectorEject,
            Provider<WarmUpShooterCommand> shooterWarmUpSupplier,
            Provider<FireCollectorCommand> fireCollectorCommandProvider,
            Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
            CalibrateArmsManuallyCommand calibrateArmsManuallyCommand
    ) {
        // Collect
        var rumbleModeFalse = new InstantCommand(() -> oi.operatorFundamentalsGamepad.getRumbleManager().stopGamepadRumble());

        var shooterWarmUpTypicalA = shooterWarmUpSupplier.get();
        var shooterWarmUpTypicalB = shooterWarmUpSupplier.get();
        var shooterWarmUpAmp = shooterWarmUpSupplier.get();

        // Fire
        shooterWarmUpTypicalA.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        shooterWarmUpTypicalB.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        shooterWarmUpAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectorEject);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightTrigger).whileTrue(collectorIntake).onFalse(rumbleModeFalse);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(scoocherEject);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(scoocherIntakeProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Start).onTrue(calibrateArmsManuallyCommand);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.A).whileTrue(shooterWarmUpAmp);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.X).whileTrue(shooterWarmUpTypicalA);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.B).whileTrue(fireCollectorCommandProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Y).whileTrue(shooterWarmUpTypicalB.alongWith(fireCollectorCommandProvider.get()));

        oi.operatorFundamentalsGamepad.getPovIfAvailable(0).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, 20));
        oi.operatorFundamentalsGamepad.getPovIfAvailable(90).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, 2));
        oi.operatorFundamentalsGamepad.getPovIfAvailable(180).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, -20));
        oi.operatorFundamentalsGamepad.getPovIfAvailable(270).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, -2));
    }

    @Inject
    public void setupAutonomousCommandSelection(OperatorInterface oi,
                                                Provider<SetAutonomousCommand> setAutonomousCommandProvider,
                                                ListenToOracleCommandGroup listenToOracleCommandGroup,
                                                SubwooferShotFromMidShootThenShootNearestThree midThenThree,
                                                SubwooferShotFromTopShootThenShootSpikes topThenThree,
                                                SubwooferShotFromBotShootThenShootSpikes botThenThree,
                                                SubwooferShotFromTopShootThenShootTopSpikeThenShootTwoCenter topThenTopSpikeTopCenter,
                                                SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter botThenBotSpikeBotCenter) {
        var setOracleAuto = setAutonomousCommandProvider.get();
        setOracleAuto.setAutoCommand(listenToOracleCommandGroup);
        oi.neoTrellis.getifAvailable(31).onTrue(setOracleAuto);

        var setMidThenThree = setAutonomousCommandProvider.get();
        setMidThenThree.setAutoCommand(midThenThree);
        oi.neoTrellis.getifAvailable(23).onTrue(setMidThenThree);
        setMidThenThree.includeOnSmartDashboard("Standard 4 Note Auto");

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

    private Command createArmFineAdjustmentCommand(Provider<SetArmExtensionCommand> commandProvider, double targetExtensionDeltaInMm) {
        var command = commandProvider.get();
        command.setTargetExtension(targetExtensionDeltaInMm);
        command.setRelative(true);
        return command;
    }
}
