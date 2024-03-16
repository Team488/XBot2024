package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.DriveToGivenNoteAndCollectCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.drive.commands.PointAtSpeakerCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Array;
import java.util.ArrayList;

public class DistanceShotFromBotThenThreeCenter extends SequentialCommandGroup {
    final AutonomousCommandSelector autoSelector;
    @Inject
    public DistanceShotFromBotThenThreeCenter(AutonomousCommandSelector autoSelector,PoseSubsystem pose,
                                              Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                                              Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                              Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
                                              Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
                                              FireFromSubwooferCommandGroup fireFromSubwooferCommandGroup){
        this.autoSelector = autoSelector;

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        // Fire note into the speaker from starting position
        queueMessageToAutoSelector("Shoot pre-loaded note from subwoofer (center)");
        // Fire preload note into the speaker from starting position
        this.addCommands(fireFromSubwooferCommandGroup);

        //swap drive and collect sequence for testing
        queueMessageToAutoSelector("Drive to Centerline4 collect and shoot");
        var driveToCenterline4 = driveToListOfPointsCommandProvider.get();
        driveToCenterline4.addPointsSupplier(this::goToFirstNote);
        driveBackwards(driveToCenterline4);
        var collectSequence1 = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(driveToCenterline4,collectSequence1));

        //drives back to shooting position and fire note
        var driveToShootingPosition1 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition1.addPointsSupplier(this::goToCenterSpike);

        var shootFirstNote = fireNoteCommandGroupProvider.get();
        this.addCommands(driveToShootingPosition1,shootFirstNote);

        //swap drive and collect for testing
        var driveToCenterline3 = driveToListOfPointsCommandProvider.get();
        driveToCenterline3.addPointsSupplier(this::goToSecondNote);
        driveBackwards(driveToCenterline3);
        var collectSequence2 = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(driveToCenterline3,collectSequence2));

        queueMessageToAutoSelector("Drive to Centerline3 collect and shoot");

        var driveToShootingPosition2 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition2.addPointsSupplier(this::goToCenterSpike);

        //drives to center note and collects and goes back to shooting position
        this.addCommands(driveToShootingPosition2);

        //fires second note
        var shootSecondNote = fireNoteCommandGroupProvider.get();
        this.addCommands(shootSecondNote);

        //swap drive and collect for testing
        var driveToCenterline2 = driveToListOfPointsCommandProvider.get();
        driveToCenterline2.addPointsSupplier(this::goToThirdNote);
        driveBackwards(driveToCenterline2);
        var collectSequence3 = collectSequenceCommandGroupProvider.get();
        this.addCommands(Commands.deadline(driveToCenterline2,collectSequence3));

        //drives and collects the third note
        queueMessageToAutoSelector("Drive to Centerline2 collect and shoot");
        var driveToShootingPosition3 = driveToListOfPointsCommandProvider.get();
        driveToShootingPosition3.addPointsSupplier(this::goToCenterSpikeFromThirdNote);
        this.addCommands(driveToShootingPosition3);

        //shoot third note
        var shootThirdNote = fireNoteCommandGroupProvider.get();
        this.addCommands(shootThirdNote);

    }
    private void queueMessageToAutoSelector(String message) {
        this.addCommands(autoSelector.createAutonomousStateMessageCommand(message));
    }
    private void driveBackwards(DriveToListOfPointsCommand drive){
        drive.logic.setAimAtGoalDuringFinalLeg(true);
        drive.logic.setDriveBackwards(true);
    }
    private ArrayList<XbotSwervePoint> goToCenterSpike(){
        var points = new ArrayList<XbotSwervePoint>();
        var translation = PoseSubsystem.BlueSpikeMiddle.getTranslation();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new
                Translation2d( 5.35, 4.4),
                Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(translation, Rotation2d.fromDegrees(180),10));
        return points;
    }
    private ArrayList<XbotSwervePoint> goToCenterSpikeFromThirdNote(){
        var points = new ArrayList<XbotSwervePoint>();
        var translation = PoseSubsystem.BlueSpikeMiddle.getTranslation();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d( 5.86, 6.4),Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(translation, Rotation2d.fromDegrees(180),10));
        return points;
    }
    private ArrayList<XbotSwervePoint> goToFirstNote(){
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(5.8674,1.5);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(translation,Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine4.getTranslation(),new Rotation2d(),10));
        return points;
    }
    private ArrayList<XbotSwervePoint> goToSecondNote(){
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(5.8674,PoseSubsystem.CenterLine3.getY());
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(translation,Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine3.getTranslation(),new Rotation2d(),10));
        return points;
    }
    private ArrayList<XbotSwervePoint> goToThirdNote(){
        var points = new ArrayList<XbotSwervePoint>();
        var translation = new Translation2d(5.8674,6.4);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(translation,Rotation2d.fromDegrees(180),10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine2.getTranslation(),new Rotation2d(),10));
        return points;
    }

}
