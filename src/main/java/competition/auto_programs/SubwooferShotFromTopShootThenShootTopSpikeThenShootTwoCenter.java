package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.DriveToGivenNoteWithVisionCommand;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.DriveToTopSubwooferCommand;
import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

public class SubwooferShotFromTopShootThenShootTopSpikeThenShootTwoCenter extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    @Inject
    public SubwooferShotFromTopShootThenShootTopSpikeThenShootTwoCenter(
            AutonomousCommandSelector autoSelector,
            Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
            Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
            Provider<DriveToTopSubwooferCommand> driveToTopSubwooferCommandProvider,
            Provider<DriveToGivenNoteWithVisionCommand> driveToNoteProvider,
            Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
            Provider<FireWhenReadyCommand> fireNoteCommandGroupProvider,
            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
            Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
            Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
            Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider,
            PoseSubsystem pose, DriveSubsystem drive
    ) {
        this.autoSelector = autoSelector;

        SetArmExtensionCommand setArmForShot4 = setArmExtensionCommandProvider.get();
        SetArmExtensionCommand setArmForShot3 = setArmExtensionCommandProvider.get();
        setArmForShot3.setTargetExtension(setArmForShot3.getArmExtensionForDistanceInmm(PointOfInterest.SpikeMiddle));
        setArmForShot4.setTargetExtension(setArmForShot4.getArmExtensionForDistanceInmm(PointOfInterest.SpikeMiddle));

        WarmUpShooterCommand warmupForShot3 = warmUpShooterCommandProvider.get();
        warmupForShot3.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        WarmUpShooterCommand warmupForShot4 = warmUpShooterCommandProvider.get();
        warmupForShot4.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);

        // Force our location
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (top)");
        var fireFirstNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireFirstNoteCommand);

        // Drive to top spike note and collect
        queueMessageToAutoSelector("Drive to top spike note, collect, drive back to sub(top) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.BlueSpikeTop);
                })
        );
        var driveToTopSpikeNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopSpikeNoteAndCollect.withTimeout(3.5));

        // Drive back to subwoofer
        var driveBackToTopSubwooferFirst = driveToTopSubwooferCommandProvider.get();
        this.addCommands(driveBackToTopSubwooferFirst);

        // Fire second note into the speaker
        var fireSecondNoteCommand = fireFromSubwooferCommandGroup.get();
        this.addCommands(fireSecondNoteCommand);

        // Drive to top center note and collect
        queueMessageToAutoSelector("Drive to top center note, collect, drive back to sub(top) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine1);
                })
        );
        var driveToTopCenterNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopCenterNoteAndCollect.withTimeout(3.5));

        var driveToShootingPosition1 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition1.addPointsSupplier(this::goToCenterSpike);

        var shootThirdNote = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(driveToShootingPosition1.withTimeout(5),shootThirdNote);
//        var shootThirdNote = fireNoteCommandGroupProvider.get();
//        this.addCommands(Commands.deadline(driveToShootingPosition1,warmupForShot3,setArmForShot3),shootThirdNote);

        queueMessageToAutoSelector("Drive to Centerline2 collect and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine2);
                })
        );
        var driveToCenterline2 = driveToNoteProvider.get();
        driveToCenterline2.setWaypoints(goToThirdNote());

        var collectSequence3 = collectSequenceCommandGroupProvider.get();

        this.addCommands(Commands.deadline(collectSequence3,driveToCenterline2).withTimeout(3.5));

        var driveToShootingPosition2 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition2.addPointsSupplier(this::goToCenterSpike);

        var shootLastNote = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(driveToShootingPosition2.withTimeout(5),shootLastNote);
//        var shootLastNote = fireNoteCommandGroupProvider.get();
//
//        this.addCommands(Commands.deadline(driveToShootingPosition2,warmupForShot4,setArmForShot4),shootLastNote);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine4);
                })
        );

        //gets us as far as we can before auto ends
        var driveToTopCenterNote = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopCenterNote.withTimeout(3.5));
    }

    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
    private Translation2d[] goToThirdNote() {
        return new Translation2d[]{
                new Translation2d(5.8674, 6.6)
        };
    }
    //goes back to subwoofer for now until we fix it
    private ArrayList<XbotSwervePoint> goToCenterSpike(){
        var points = new ArrayList<XbotSwervePoint>();
        //var translation = PoseSubsystem.BlueSpikeMiddle.getTranslation();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new
                        Translation2d( 5.86, 6.6),
                Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.BlueSubwooferTopScoringLocation.getTranslation(), Rotation2d.fromDegrees(180),10));
        return points;
    }
}
