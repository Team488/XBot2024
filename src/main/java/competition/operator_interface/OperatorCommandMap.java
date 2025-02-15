package competition.operator_interface;

import competition.auto_programs.BotCenter5ThenCenter4;
import competition.auto_programs.DoNothingAuto;
import competition.auto_programs.GriefMiddle;
import competition.auto_programs.SixNoteBnbExtended;
import competition.auto_programs.SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter;
import competition.auto_programs.SubwooferShotFromBotShootThenShootSpikes;
import competition.auto_programs.BotCenter4ThenCenter5;
import competition.auto_programs.SubwooferShotFromMidShootThenShootNearestThree;
import competition.auto_programs.SubwooferShotFromTopShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromTopShootThenShootTopSpikeThenShootTwoCenter;
import competition.commandgroups.DriveToWaypointsWithVisionCommand;
import competition.commandgroups.PrepareToFireAtSpeakerFromPodiumCommand;
import competition.commandgroups.PrepareToFireNearestGoodScoringPositionCommand;
import competition.commandgroups.PrepareToLobShotCommand;
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
import competition.subsystems.drive.commands.LineUpForHangingCommand;
import competition.subsystems.drive.commands.PointAtNoteWithBearingCommand;
import competition.subsystems.drive.commands.TryDriveToBearingNote;
import competition.subsystems.flipper.commands.SetFlipperServoToHangPositionCommand;
import competition.subsystems.lights.commands.AmpSignalToggleCommand;
import competition.subsystems.flipper.commands.ToggleFlipperCommand;
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
            PointAtNoteWithBearingCommand alignToNoteCommand,
            LineUpForHangingCommand lineUpForHangingCommand,
            DriveToAmpCommand driveToAmpCommand,
            ListenToOracleCommandGroup listenToOracleCommandGroup,
            DriveToWaypointsWithVisionCommand driveToWaypointsWithVisionCommand,
            LimitArmToUnderStage limitArmToUnderStageCommand)
    {
        // Rotation calibration routine
        resetHeading.setHeadingToApply(() -> PoseSubsystem.convertBlueToRedIfNeeded(Rotation2d.fromDegrees(180)).getDegrees());

        var pointAtSpeaker = drive.createSetSpecialPointAtPositionTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_TARGET_FORWARD));

        var pointAtSource = drive.createSetSpecialHeadingTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.FaceCollectorToBlueSource));

        var pointAtAmpForLob = drive.createSetSpecialHeadingTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.FaceShooterToBlueAmpForLob));

        var cancelSpecialPointAtPosition = drive.createClearAllSpecialTargetsCommand();

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(listenToOracleCommandGroup);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(resetHeading);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(pointAtAmpForLob).onFalse(cancelSpecialPointAtPosition);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(driveToWaypointsWithVisionCommand);
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
            AmpSignalToggleCommand ampSignalCommand,
            ToggleFlipperCommand toggleFlipperCommand,
            PrepareToLobShotCommand prepareToLobShotCommand,
            Provider<WarmUpShooterCommand> shooterWarmUpSupplier
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
        warmUpShooterSubwoofer.setTargetRpm(ShooterWheelSubsystem.TargetRPM.MELEE);

        var warmUpShooterToFireInAmp = warmUpShooterCommandProvider.get();
        warmUpShooterToFireInAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);

        var shooterWarmUpTypical = shooterWarmUpSupplier.get();
        shooterWarmUpTypical.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);

        // Combine into useful actions
        // Note manipulation:
        var collectNoteFromGround = intakeCollectorProvider.get().alongWith(armToCollection);
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
        oi.operatorGamepadAdvanced.getPovIfAvailable(0).whileTrue(prepareToLobShotCommand);
        oi.operatorGamepadAdvanced.getPovIfAvailable(180).whileTrue(manualHangingModeCommand);
        oi.operatorGamepadAdvanced.getPovIfAvailable(270).whileTrue(ampSignalCommand);
        oi.operatorGamepadAdvanced.getPovIfAvailable(90).whileTrue(toggleFlipperCommand);
    }

    @Inject
    public void setupFundamentalCommands(
            OperatorInterface oi,
            Provider<IntakeScoocherCommand> scoocherIntakeProvider,
            SetFlipperServoToHangPositionCommand setFlipperServoToHangPositionCommand,
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
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftBumper).onTrue(setFlipperServoToHangPositionCommand);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(scoocherIntakeProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Start).onTrue(calibrateArmsManuallyCommand);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.A).whileTrue(shooterWarmUpAmp);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.X).whileTrue(shooterWarmUpTypicalA);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.B).whileTrue(fireCollectorCommandProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Y).whileTrue(shooterWarmUpTypicalB.alongWith(fireCollectorCommandProvider.get()));

        oi.operatorFundamentalsGamepad.getPovIfAvailable(0).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, 20));
        oi.operatorFundamentalsGamepad.getPovIfAvailable(90).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, 1.2));
        oi.operatorFundamentalsGamepad.getPovIfAvailable(180).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, -20));
        oi.operatorFundamentalsGamepad.getPovIfAvailable(270).whileTrue(createArmFineAdjustmentCommand(setArmExtensionCommandProvider, -1.2));
    }

    @Inject
    public void setupAutonomousCommandSelection(OperatorInterface oi,
                                                Provider<SetAutonomousCommand> setAutonomousCommandProvider,
                                                ListenToOracleCommandGroup listenToOracleCommandGroup,
                                                SubwooferShotFromMidShootThenShootNearestThree midThenThree,
                                                SubwooferShotFromTopShootThenShootSpikes topThenThree,
                                                SubwooferShotFromBotShootThenShootSpikes botThenThree,
                                                SubwooferShotFromTopShootThenShootTopSpikeThenShootTwoCenter topThenTopSpikeTopCenter,
                                                SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter botThenBotSpikeBotCenter,
                                                SixNoteBnbExtended bnbExtended,
                                                DoNothingAuto doNothing,
                                                GriefMiddle grief,
                                                BotCenter4ThenCenter5 botCenter4ThenCenter5,
                                                BotCenter5ThenCenter4 botCenter5ThenCenter4,
                                                ArmSubsystem arm) {
        var setOracleAuto = setAutonomousCommandProvider.get();
        setOracleAuto.setAutoCommand(listenToOracleCommandGroup);
        oi.neoTrellis.getifAvailable(31).onTrue(setOracleAuto);

        var setMidThenThree = setAutonomousCommandProvider.get();
        setMidThenThree.setAutoCommand(midThenThree);
        oi.neoTrellis.getifAvailable(23).onTrue(setMidThenThree);
        setMidThenThree.includeOnSmartDashboard("Standard 4 Note Auto");

        var setGrief = setAutonomousCommandProvider.get();
        setGrief.setAutoCommand(grief);
        oi.neoTrellis.getifAvailable(15).onTrue(setGrief);

        var setBotThenThree = setAutonomousCommandProvider.get();
        setBotThenThree.setAutoCommand(botThenThree);
        oi.neoTrellis.getifAvailable(7).onTrue(setBotThenThree);

        var setTopThenTopSpikeTopCenter = setAutonomousCommandProvider.get();
        setTopThenTopSpikeTopCenter.setAutoCommand(topThenTopSpikeTopCenter);
        oi.neoTrellis.getifAvailable(32).onTrue(setTopThenTopSpikeTopCenter);

        var setBotThenBotSpikeBotCenter = setAutonomousCommandProvider.get();
        setBotThenBotSpikeBotCenter.setAutoCommand(botThenBotSpikeBotCenter);
        oi.neoTrellis.getifAvailable(24).onTrue(setBotThenBotSpikeBotCenter);

        var setBnbExtended = setAutonomousCommandProvider.get();
        setBnbExtended.setAutoCommand(bnbExtended);
        oi.neoTrellis.getifAvailable(16).onTrue(setBnbExtended);

        var setDoNothing = setAutonomousCommandProvider.get();
        setDoNothing.setAutoCommand(doNothing);
        oi.neoTrellis.getifAvailable(8).onTrue(setDoNothing);

        var setbotCenter4ThenCenter5 = setAutonomousCommandProvider.get();
        setbotCenter4ThenCenter5.setAutoCommand(botCenter4ThenCenter5);
        oi.neoTrellis.getifAvailable(30).onTrue(setbotCenter4ThenCenter5);

        var setbotCenter5ThenCenter4 = setAutonomousCommandProvider.get();
        setbotCenter5ThenCenter4.setAutoCommand(botCenter5ThenCenter4);
        oi.neoTrellis.getifAvailable(22).onTrue(setbotCenter5ThenCenter4);



        oi.neoTrellis.getifAvailable(1).onTrue(new InstantCommand(() -> arm.increaseTrimInMeters(0.1524)));
        oi.neoTrellis.getifAvailable(9).onTrue(new InstantCommand(() -> arm.increaseTrimInMeters(-0.1524)));
    }

    @Inject
    public void setupTestingCommands(TryDriveToBearingNote tryDriveToBearingNote) {
        tryDriveToBearingNote.includeOnSmartDashboard();
    }

    private Command createArmFineAdjustmentCommand(Provider<SetArmExtensionCommand> commandProvider, double targetExtensionDeltaInMm) {
        var command = commandProvider.get();
        command.setTargetExtension(targetExtensionDeltaInMm);
        command.setRelative(true);
        return command;
    }
}
