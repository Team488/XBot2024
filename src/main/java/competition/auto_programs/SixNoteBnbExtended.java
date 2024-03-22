package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.DriveToGivenNoteWithVisionCommand;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
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

//six note auto that combines my top auto with the bnb auto
public class SixNoteBnbExtended extends SequentialCommandGroup {
    final AutonomousCommandSelector autoSelector;
    @Inject
    public SixNoteBnbExtended(SubwooferShotFromMidShootThenShootNearestThree bnbAuto,
                              AutonomousCommandSelector autoSelector,
                              Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                              Provider<DriveToGivenNoteWithVisionCommand> driveToNoteProvider,
                              Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
                              Provider<FireWhenReadyCommand> fireNoteCommandGroupProvider,
                              Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
                              Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
                              Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                              Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider,
                              DriveSubsystem drive){
        this.autoSelector = autoSelector;

//        SetArmExtensionCommand setArmForShot1 = setArmExtensionCommandProvider.get();
//        SetArmExtensionCommand setArmForShot2 = setArmExtensionCommandProvider.get();
//        setArmForShot2.setTargetExtension(setArmForShot2.getArmExtensionForDistanceInmm(PointOfInterest.SpikeMiddle));
//        setArmForShot1.setTargetExtension(setArmForShot1.getArmExtensionForDistanceInmm(PointOfInterest.SpikeMiddle));

        WarmUpShooterCommand warmupForShot1 = warmUpShooterCommandProvider.get();
        warmupForShot1.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);
        WarmUpShooterCommand warmupForShot2 = warmUpShooterCommandProvider.get();
        warmupForShot2.setTargetRpm(ShooterWheelSubsystem.TargetRPM.TYPICAL);

        //THIS GETS THE FIRST FOUR NOTES
        this.addCommands(bnbAuto);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine1);
                })
        );

        var driveToTopCenterNoteAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToTopCenterNoteAndCollect.withTimeout(3.5));

        var driveToShootingPosition1 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition1.addPointsSupplier(this::goToCenterSpike);

        var shootFifthNote = fireFromSubwooferCommandGroupProvider.get();

        this.addCommands(driveToShootingPosition1.withTimeout(5),shootFifthNote);
        //this.addCommands(Commands.deadline(driveToShootingPosition1,warmupForShot1),shootFifthNote);

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

        var shootLastNote = fireNoteCommandGroupProvider.get();

        this.addCommands(Commands.deadline(driveToShootingPosition2,warmupForShot2),shootLastNote);

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
    private ArrayList<XbotSwervePoint> goToCenterSpike(){
        var points = new ArrayList<XbotSwervePoint>();
        //var translation = PoseSubsystem.BlueSpikeMiddle.getTranslation();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new
                        Translation2d( 5.86, 6.6),
                Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSubwooferMiddleScoringLocation.getTranslation(), Rotation2d.fromDegrees(180),10));
        return points;
    }
}
