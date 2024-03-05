package competition.operator_interface;

import competition.auto_programs.SubwooferShotFromBotShootThenShootBotSpikeThenShootBotCenter;
import competition.auto_programs.SubwooferShotFromBotShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromMidShootThenShootNearestThree;
import competition.auto_programs.SubwooferShotFromTopShootThenShootSpikes;
import competition.auto_programs.SubwooferShotFromTopShootThenShootTopSpikeThenShootTopCenter;
import competition.commandgroups.PrepareToFireAtSpeakerFromFarAmpCommand;
import competition.commandgroups.PrepareToFireAtSpeakerFromPodiumCommand;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.CalibrateArmsManuallyCommand;
import competition.subsystems.arm.commands.ContinuouslyPointArmAtSpeakerCommand;
import competition.subsystems.arm.commands.ForceEngageBrakeCommand;
import competition.subsystems.arm.commands.ManualHangingModeCommand;
import competition.subsystems.arm.commands.RemoveForcedBrakingCommand;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.FireCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.AlignToNoteCommand;
import competition.subsystems.drive.commands.DriveToAmpCommand;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
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
            AlignToNoteCommand alignToNoteCommand,
            DriveToCentralSubwooferCommand driveToCentralSubwooferCommand,
            DriveToAmpCommand driveToAmpCommand,
            ListenToOracleCommandGroup listenToOracleCommandGroup
            )
    {
        // Rotation calibration routine
        resetHeading.setHeadingToApply(() -> PoseSubsystem.convertBlueToRedIfNeeded(Rotation2d.fromDegrees(180)).getDegrees());

        var pointAtSpeaker = drive.createSetSpecialPointAtPositionTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_POSITION));

        var pointAtSource = drive.createSetSpecialHeadingTargetCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.FaceCollectorToBlueSource));

        operatorInterface.driverGamepad.getXboxButton(XboxButton.Back).whileTrue(listenToOracleCommandGroup);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Start).onTrue(resetHeading);
        //drive to podium
//        operatorInterface.driverGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue();
        operatorInterface.driverGamepad.getXboxButton(XboxButton.B).whileTrue(pointAtSpeaker);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.A).whileTrue(alignToNoteCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(driveToAmpCommand);
        operatorInterface.driverGamepad.getXboxButton(XboxButton.Y).onTrue(pointAtSource);
        //drive and orient to hang
//        operatorInterface.driverGamepad.getXboxButton(XboxButton.X).onTrue();
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
            PrepareToFireAtSpeakerFromFarAmpCommand prepareToFireAtSpeakerFromFarAmp,
            ManualHangingModeCommand manualHangingModeCommand,
            ForceEngageBrakeCommand forceEngageBrakeCommand,
            RemoveForcedBrakingCommand removeForcedBrakingCommand
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
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Start).whileTrue(ejectScoocher);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.A).whileTrue(prepareToFireAtAmp);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.B).whileTrue(prepareToFireAtSpeakerFromFarAmp);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.X).whileTrue(prepareToFireAtSpeakerFromPodium);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.Y).whileTrue(prepareToFireAtSubwoofer);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightJoystickYAxisPositive).onTrue(forceEngageBrakeCommand);
        oi.operatorGamepadAdvanced.getXboxButton(XboxButton.RightJoystickYAxisNegative).onTrue(removeForcedBrakingCommand);
        oi.operatorGamepadAdvanced.getPovIfAvailable(0).whileTrue(collectNoteFromSource);
        oi.operatorGamepadAdvanced.getPovIfAvailable(180).whileTrue(manualHangingModeCommand);
    }

    @Inject
    public void setupFundamentalCommands(
            OperatorInterface oi,
            Provider<IntakeScoocherCommand> scoocherIntakeProvider,
            EjectScoocherCommand scoocherEject,
            IntakeCollectorCommand collectorIntake,
            EjectCollectorCommand collectorEject,
            WarmUpShooterCommand shooterWarmUpTypical,
            WarmUpShooterCommand shooterWarmUpAmp,
            FireCollectorCommand fireCollectorCommand,
            Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
            CalibrateArmsManuallyCommand calibrateArmsManuallyCommand
    ) {
        // Collect
        var rumbleModeFalse = new InstantCommand(() -> oi.operatorFundamentalsGamepad.getRumbleManager().stopGamepadRumble());

        // Fire
        shooterWarmUpTypical.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        shooterWarmUpAmp.setTargetRpm(ShooterWheelSubsystem.TargetRPM.INTO_AMP);

        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftTrigger).whileTrue(collectorEject);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightTrigger).whileTrue(collectorIntake).onFalse(rumbleModeFalse);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.LeftBumper).whileTrue(scoocherEject);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.RightBumper).whileTrue(scoocherIntakeProvider.get());
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Back).whileTrue(shooterWarmUpAmp);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.Start).onTrue(calibrateArmsManuallyCommand);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.A).whileTrue(shooterWarmUpTypical);
        oi.operatorFundamentalsGamepad.getXboxButton(XboxButton.B).whileTrue(fireCollectorCommand);

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
    
    @Inject
    public void setupForceRobotToPositionCommands(PoseSubsystem pose,
                                                  OperatorInterface oi) {
        var teleportRobotToSubwooferTop = pose.createSetPositionCommandThatMirrorsIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation);
        oi.neoTrellis.getifAvailable(17).onTrue(teleportRobotToSubwooferTop);

        var teleportRobotToSubwooferMid = pose.createSetPositionCommandThatMirrorsIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation);
        oi.neoTrellis.getifAvailable(18).onTrue(teleportRobotToSubwooferMid);

        var teleportRobotToSubwooferBottom = pose.createSetPositionCommandThatMirrorsIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation);
        oi.neoTrellis.getifAvailable(19).onTrue(teleportRobotToSubwooferBottom);
    }


    private Command createArmFineAdjustmentCommand(Provider<SetArmExtensionCommand> commandProvider, double targetExtensionDeltaInMm) {
        var command = commandProvider.get();
        command.setTargetExtension(targetExtensionDeltaInMm);
        command.setRelative(true);
        return command;
    }

}