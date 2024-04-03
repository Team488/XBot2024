package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.DriveToGivenNoteWithVisionCommand;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToBottomSubwooferCommand;
import competition.subsystems.drive.commands.DriveToGivenNoteWithBearingVisionCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.pose.PoseSubsystem;
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

//three note centerline auto, centerline 5 then 4, fires from subwoofer for now
//when we have the time to tune a ranged shot will update to that
public class SubwooferShotFromBotThenTwoCenterline extends SequentialCommandGroup {
    AutonomousCommandSelector autoSelector;
    double centerlineTimeout = 8;
    @Inject
    public SubwooferShotFromBotThenTwoCenterline(AutonomousCommandSelector autoSelector, PoseSubsystem pose,
                                                 DriveSubsystem drive,
                                                 Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider,
                                                 Provider<DriveToGivenNoteWithBearingVisionCommand> driveToNoteProvider,
                                                 Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
                                                 Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                                 Provider<IntakeCollectorCommand> intakeCollectorCommandProvider
                                                 ){
        this.autoSelector = autoSelector;

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (middle)");
        var fireFirstNoteCommand = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(fireFirstNoteCommand);

        // Drive to centerline5 note and collect
        queueMessageToAutoSelector("Drive to bottom spike note, collect, drive back to sub(middle) and shoot");
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine5);
                })
        );
        var driveToCenterline5 = driveToNoteProvider.get();
        this.addCommands(
                new InstantCommand(()->{
                    driveToCenterline5.setWaypoints(new Translation2d(
                            PoseSubsystem.BlueSpikeBottom.getX() + 2.06,
                            PoseSubsystem.BlueSpikeBottom.getY() - 2.3951
                    ));
                })
        );
        driveToCenterline5.logic.setEnableConstantVelocity(true);
        driveToCenterline5.setMaximumSpeedOverride(drive.getSuggestedAutonomousExtremeSpeed());

        var collect1 = collectSequenceCommandGroupProvider.get();
        //swap collect and drive for testing
        this.addCommands(Commands.deadline(collect1,driveToCenterline5).withTimeout(centerlineTimeout));

        var driveBackToBottomSubwooferFirst = driveToListOfPointsCommandProvider.get();
        driveBackToBottomSubwooferFirst.addPointsSupplier(this::goBackToBotSubwoofer);
        driveBackToBottomSubwooferFirst.setMaximumSpeedOverride(drive.getSuggestedAutonomousExtremeSpeed());

        var collect3 = intakeCollectorCommandProvider.get();
        this.addCommands(Commands.deadline(driveBackToBottomSubwooferFirst.withTimeout(centerlineTimeout),collect3));

        // Fire second note into the speaker
        var fireSecondNoteCommand = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(fireSecondNoteCommand);

        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(PoseSubsystem.CenterLine4);
                })
        );
        var driveToCenterline4 = driveToNoteProvider.get();
        this.addCommands(
                new InstantCommand(()->{
                    driveToCenterline4.setWaypoints(new Translation2d(
                            PoseSubsystem.BlueSpikeBottom.getX() + 2.06,
                            PoseSubsystem.BlueSpikeBottom.getY() - 2.3951
                    ));
                })
        );

        driveToCenterline4.logic.setEnableConstantVelocity(true);
        driveToCenterline4.setMaximumSpeedOverride(drive.getSuggestedAutonomousExtremeSpeed());

        var collect2 = collectSequenceCommandGroupProvider.get();
        //swap collect and drive for testing
        this.addCommands(Commands.deadline(collect2,driveToCenterline4).withTimeout(centerlineTimeout));

        var driveBackToBottomSubwooferSecond = driveToListOfPointsCommandProvider.get();
        driveBackToBottomSubwooferSecond.addPointsSupplier(this::goBackToBotSubwoofer);
        driveBackToBottomSubwooferSecond.setMaximumSpeedOverride(drive.getSuggestedAutonomousExtremeSpeed());

        var collect4 = intakeCollectorCommandProvider.get();
        this.addCommands(Commands.deadline(driveBackToBottomSubwooferSecond.withTimeout(centerlineTimeout),collect4));

        // Fire second note into the speaker
        var fireThirdNoteCommand = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(fireThirdNoteCommand);

    }
    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
    private ArrayList<XbotSwervePoint> goBackToBotSubwoofer(){
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(
                        PoseSubsystem.BlueSpikeBottom.getX() + 2.06,
                        PoseSubsystem.BlueSpikeBottom.getY() - 2.3951), Rotation2d.fromDegrees(140),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSubwooferBottomScoringLocation,10));
        return points;
    }
}
