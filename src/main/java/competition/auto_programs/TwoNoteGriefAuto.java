package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import competition.subsystems.schoocher.commands.IntakeScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

//am i cookin with this?? 🤫🧏‍♂️🤑
//this auto starts in front of the subwoofer
//scores the note we are holding, then goes to the center and steals the notes to our side via both the scoocher and the collector
//at the end, it grabs the last centerline note and scores it at the subwoofer.
public class TwoNoteGriefAuto extends SequentialCommandGroup {
    final DynamicOracle oracle;
    final AutonomousCommandSelector autoSelector;
    private enum KeyPointsForSwerve{
        CENTERLINE1,
        CENTERLINE4,
        LASTNOTE,
        BACKTOSUBWOOFER
    }
    @Inject
    public TwoNoteGriefAuto(Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
                            Provider<SetArmAngleCommand> setArmAngleProvider,
                            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
                            Provider<FireWhenReadyCommand> fireWhenReadyCommandProvider,
                            IntakeScoocherCommand scoochIntake,
                            EjectScoocherCommand scoochEject,
                            IntakeUntilNoteCollectedCommand intakeUntilCollected,
                            PoseSubsystem pose,
                            DynamicOracle oracle,
                            AutonomousCommandSelector autoSelector){
        this.autoSelector = autoSelector;
        this.oracle = oracle;

        //starts us in front of the subwoofer, to score
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SubwooferCentralScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        //fires the note we are holding at the start
        var warmUpForFirstSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForFirstSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        var fireFirstShot = fireWhenReadyCommandProvider.get();

        this.addCommands(Commands.deadline(fireFirstShot,
                warmUpForFirstSubwooferShot));

        //gets arm into scooching position and sets up swerve pathing to mess up center notes
        var setArmToScooch = setArmAngleProvider.get();
        setArmToScooch.setArmPosition(ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE);

        var swerveToEdge = swerveProvider.get();
        setUpLogic(swerveToEdge,KeyPointsForSwerve.CENTERLINE1);
        swerveToEdge.logic.setAimAtGoalDuringFinalLeg(true);
        var swerveToOtherEdgeWhileStrafing = swerveProvider.get();

        setUpLogic(swerveToOtherEdgeWhileStrafing,KeyPointsForSwerve.CENTERLINE4);

        //scooches first note, then reverse scooches the rest
        this.addCommands(Commands.deadline(swerveToEdge,scoochIntake,setArmToScooch));
        this.addCommands(Commands.deadline(swerveToOtherEdgeWhileStrafing,scoochEject));

        //sets arm and collects Centerline 5 note
        var swerveLastNote = swerveProvider.get();
        setUpLogic(swerveLastNote,KeyPointsForSwerve.LASTNOTE);
        swerveLastNote.logic.setAimAtGoalDuringFinalLeg(true);
        swerveLastNote.logic.setDriveBackwards(true);

        var setArmToCollect = setArmAngleProvider.get();
        setArmToCollect.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        //swap the swerve and the intake later this is just for simulator
        this.addCommands(setArmToCollect.alongWith(Commands.deadline(swerveLastNote,intakeUntilCollected)));

        //drives back to the subwoofer and scores
        var driveToSubwoofer = swerveProvider.get();
        setUpLogic(driveToSubwoofer,KeyPointsForSwerve.BACKTOSUBWOOFER);

        var warmUpForSecondSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForSecondSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        var fireSecondShot = fireWhenReadyCommandProvider.get();

        this.addCommands(Commands.deadline(driveToSubwoofer, warmUpForSecondSubwooferShot));
        this.addCommands(fireSecondShot);

    }
    //sets up basic logic when given a swerve command
    private void setUpLogic(SwerveSimpleTrajectoryCommand swerve,KeyPointsForSwerve point){
        swerve.logic.setEnableConstantVelocity(true);
        swerve.logic.setConstantVelocity(4.5);
        swerve.logic.setKeyPoints(getPoints(point));
        swerve.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
    }
    //a key to make this function return different points based on what you need
    private ArrayList<XbotSwervePoint> getPoints(KeyPointsForSwerve point){
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        switch (point){
            //drives to the first centerline note with an angle ready to collect
            case CENTERLINE1 -> points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine1, 10));
            //drives through middle 3 notes on the way to the fourth note
            case CENTERLINE4 ->
                    points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.45,2.388), Rotation2d.fromDegrees(65),10));
            case LASTNOTE -> points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.296,0.6),new Rotation2d(),10));
            //the case below doesnt convert to red so I just manually added the red point for now, change it to the comment if fixed
            //case BACKTOSUBWOOFER -> points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.SubwooferCentralScoringLocation,10));
            case BACKTOSUBWOOFER -> points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(15.2,5.553),new Rotation2d(),10));
            default -> autoSelector.createAutonomousStateMessageCommand("Ayo you typed something wrong");
        }
        return points;
    }
}
