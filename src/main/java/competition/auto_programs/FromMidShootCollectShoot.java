package competition.auto_programs;

import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.drive.commands.StopDriveCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

public class FromMidShootCollectShoot extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public FromMidShootCollectShoot(
            PoseSubsystem pose,
            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
            Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
            Provider<FireWhenReadyCommand> fireWhenReadyCommandProvider,
            IntakeCollectorCommand intake,
            EjectCollectorCommand eject,
            StopCollectorCommand stopCollector,
            StopDriveCommand stopDrive,
            AutonomousCommandSelector autoSelector) {
        this.autoSelector = autoSelector;
        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferCentralScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Shoot the pre-loaded note from the subwoofer
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer");
        var warmUpForFirstSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForFirstSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        var fireFirstShot = fireWhenReadyCommandProvider.get();

        this.addCommands(Commands.deadline(fireFirstShot,
                warmUpForFirstSubwooferShot));

        // Drive to the middle note while collecting
        queueMessageToAutoSelector("Drive to middle note");
        var driveToMiddleSpike = swerveProvider.get();
        driveToMiddleSpike.logic.setEnableConstantVelocity(true);
        driveToMiddleSpike.logic.setConstantVelocity(1);
        driveToMiddleSpike.logic.setAimAtGoalDuringFinalLeg(true);
        driveToMiddleSpike.logic.setKeyPointsProvider(() -> {
            ArrayList<XbotSwervePoint> points = new ArrayList<>();
            var target = BasePoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SpikeMiddle);
            points.add(new XbotSwervePoint(target.getTranslation(), target.getRotation(), 10));
            return points;
        });
        var stopShooter = warmUpShooterCommandProvider.get();
        stopShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.STOP);

        this.addCommands(Commands.deadline(driveToMiddleSpike,
                intake, stopShooter));

        // Return to subwoofer, warm up shooter)
        queueMessageToAutoSelector("Return to subwoofer");
        var driveToSubwoofer = swerveProvider.get();
        driveToSubwoofer.logic.setEnableConstantVelocity(true);
        driveToSubwoofer.logic.setConstantVelocity(1);
        driveToSubwoofer.logic.setKeyPointsProvider(() -> {
            ArrayList<XbotSwervePoint> points = new ArrayList<>();
            var target = BasePoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferCentralScoringLocation);
            points.add(new XbotSwervePoint(target.getTranslation(), target.getRotation(), 10));
            return points;
        });

        var warmUpForSecondSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForSecondSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);

        this.addCommands(Commands.deadline(driveToSubwoofer,
                warmUpForSecondSubwooferShot));

        // Fire note
        queueMessageToAutoSelector("Fire second note");
        var fireSecondShot = fireWhenReadyCommandProvider.get();
        this.addCommands(Commands.deadline(fireSecondShot,
                stopDrive));
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
}
