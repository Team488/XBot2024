package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.PointAtSpeakerCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

public class DistanceShotFromBotThenThreeCenter extends SequentialCommandGroup {
    final AutonomousCommandSelector autoSelector;
    @Inject
    public DistanceShotFromBotThenThreeCenter(AutonomousCommandSelector autoSelector,
                                              Provider<DriveToGivenNoteAndCollectCommandGroup> driveToGivenNoteAndCollectCommandGroupProvider,
                                              Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                                              Provider<DriveToCentralSubwooferCommand> driveToCentralSubwooferCommandProvider,
                                              PoseSubsystem pose, DriveSubsystem drive,
                                              Provider<PointAtSpeakerCommand> pointAtSpeakerCommandProvider,
                                              FireFromSubwooferCommandGroup fireFromSubwooferCommandGroup,
                                              Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                              Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider){
        this.autoSelector = autoSelector;

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (center)");
        // Fire preload note into the speaker from starting position
        this.addCommands(fireFromSubwooferCommandGroup);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine4);
                })
        );

        queueMessageToAutoSelector("Drive to Centerline4 collect and shoot");
        //drive and collects second note
        var driveToFourthCenterAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        this.addCommands(driveToFourthCenterAndCollect);

        //drives back to shooting position and fire note
        var driveToShootingPosition1 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition1.addPointsSupplier(this::goToUnderPodium);

        var shootFirstNote = fireNoteCommandGroupProvider.get();
        this.addCommands(driveToShootingPosition1,shootFirstNote);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine3);
                })
        );
        queueMessageToAutoSelector("Drive to Centerline3 collect and shoot");
        var driveToThirdCenterAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        var driveToShootingPosition2 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition2.addPointsSupplier(this::goToUnderPodium);

        //drives to center note and collects and goes back to shooting position
        this.addCommands(driveToThirdCenterAndCollect,driveToShootingPosition2);

        //fires second note
        var shootSecondNote = fireNoteCommandGroupProvider.get();
        this.addCommands(shootSecondNote);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine2);
                })
        );
        //drives and collects the third note
        queueMessageToAutoSelector("Drive to Centerline2 collect and shoot");
        var driveToSecondCenterAndCollect = driveToGivenNoteAndCollectCommandGroupProvider.get();
        var driveToShootingPosition3 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition3.addPointsSupplier(this::goToUnderPodium);
        this.addCommands(driveToSecondCenterAndCollect,driveToShootingPosition3);

        //shoot third note
        var shootThirdNote = fireNoteCommandGroupProvider.get();
        this.addCommands(shootThirdNote);

    }
    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
    private ArrayList<XbotSwervePoint> goToUnderPodium(){
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(4.3, 4.919);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(translation, Rotation2d.fromDegrees(180),10));
        return points;
    }

}
