package competition.commandgroups;

import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToGivenNoteWithBearingVisionCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionRange;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.subsystems.drive.swerve.SwerveDriveSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

public class GoToGenericMidline extends SequentialCommandGroup {

    protected AutonomousCommandSelector autoSelector;
    protected double centerlineTimeout = 999;
    protected double meterThreshold = 0.3048;
    protected double velocityThreshold = 0.01;
    protected PoseSubsystem pose;
    protected DriveSubsystem drive;

    protected Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider;
    protected Provider<DriveToGivenNoteWithBearingVisionCommand> driveToNoteProvider;
    protected Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider;
    protected Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider;
    protected Provider<IntakeCollectorCommand> intakeCollectorCommandProvider;


    @Inject
    public GoToGenericMidline(
            DriveSubsystem drive,
            PoseSubsystem pose,
            AutonomousCommandSelector autoSelector,
            Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider,
            Provider<DriveToGivenNoteWithBearingVisionCommand> driveToNoteProvider,
            Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
            Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
            Provider<IntakeCollectorCommand> intakeCollectorCommandProvider) {
        this.pose = pose;
        this.drive = drive;
        this.autoSelector = autoSelector;

        this.fireFromSubwooferCommandGroupProvider = fireFromSubwooferCommandGroupProvider;
        this.driveToNoteProvider = driveToNoteProvider;
        this.collectSequenceCommandGroupProvider = collectSequenceCommandGroupProvider;
        this.driveToListOfPointsCommandProvider = driveToListOfPointsCommandProvider;
        this.intakeCollectorCommandProvider = intakeCollectorCommandProvider;
    }

    protected void setupStart() {
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire preload note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (middle)");
        var fireFirstNoteCommand = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(fireFirstNoteCommand);
    }

    protected void collectCenterNoteReturnAndFire(Pose2d centerNoteLocation) {
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(centerNoteLocation);
                })
        );
        var drivetoCenterLine = driveToNoteProvider.get();
        this.addCommands(
                new InstantCommand(()->{
                    drivetoCenterLine.setWaypoints(new Translation2d(
                            PoseSubsystem.BlueSpikeBottom.getX() + 2.06,
                            PoseSubsystem.BlueSpikeBottom.getY() - 2.3951
                    ));
                })
        );
        drivetoCenterLine.logic.setEnableConstantVelocity(true);
        drivetoCenterLine.setMaximumSpeedOverride(drive.getSuggestedAutonomousExtremeSpeed());
        drivetoCenterLine.setVisionRangeOverride(VisionRange.Far);

        var collectSequenceCommand = collectSequenceCommandGroupProvider.get();
        //swap collect and drive for testing
        this.addCommands(Commands.deadline(collectSequenceCommand,drivetoCenterLine).withTimeout(centerlineTimeout));

        addCommands(drive.createChangeDriveCurrentLimitsCommand(SwerveDriveSubsystem.CurrentLimitMode.Teleop));

        var driveBackToBottomSubwoofer = driveToListOfPointsCommandProvider.get();
        driveBackToBottomSubwoofer.addPointsSupplier(this::goBackToBotSubwoofer);
        driveBackToBottomSubwoofer.setMaximumSpeedOverride(drive.getSuggestedAutonomousExtremeSpeed());
        driveBackToBottomSubwoofer.setAlternativeIsFinishedSupplier(this::alternativeIsFinishedForSubwoofer);

        var collectAgain = intakeCollectorCommandProvider.get();
        this.addCommands(Commands.deadline(driveBackToBottomSubwoofer.withTimeout(centerlineTimeout),collectAgain));

        // Fire second note into the speaker
        var fireNoteCommand = fireFromSubwooferCommandGroupProvider.get();
        this.addCommands(fireNoteCommand);
    }

    protected void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
    protected ArrayList<XbotSwervePoint> goBackToBotSubwoofer(){
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Translation2d(
                        PoseSubsystem.BlueSpikeBottom.getX() + 2.06,
                        PoseSubsystem.BlueSpikeBottom.getY() - 2.3951), Rotation2d.fromDegrees(140),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueSubwooferBottomScoringLocation,10));
        return points;
    }

    protected boolean alternativeIsFinishedForSubwoofer() {

        double speed = pose.getRobotCurrentSpeed();

        Translation2d robotLocation = pose.getCurrentPose2d().getTranslation();

        // Returns finished if both position and velocity are under threshold
        boolean nearPositionThreshold = PoseSubsystem.convertBlueToRedIfNeeded(
                        PoseSubsystem.BlueSubwooferBottomScoringLocation)
                .getTranslation().getDistance(robotLocation) < meterThreshold;

        boolean nearVelocityThreshold = speed < velocityThreshold;

        return (nearPositionThreshold && nearVelocityThreshold);
    }
}
