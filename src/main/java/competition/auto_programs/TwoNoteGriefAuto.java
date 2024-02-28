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
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

//am i cookin with this?? 🤫🧏‍♂️🤑
public class TwoNoteGriefAuto extends SequentialCommandGroup {
    DynamicOracle oracle;
    @Inject
    public TwoNoteGriefAuto(Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
                            Provider<SetArmAngleCommand> setArmAngleProvider,
                            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
                            Provider<FireWhenReadyCommand> fireWhenReadyCommandProvider,
                            IntakeScoocherCommand scoochIntake,
                            EjectScoocherCommand scoochEject,
                            IntakeUntilNoteCollectedCommand intakeUntilCollected,
                            EjectCollectorCommand eject,
                            StopCollectorCommand stopCollector,
                            PoseSubsystem pose,
                            DynamicOracle oracle){
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
        setUpLogic(swerveToEdge,1);
        swerveToEdge.logic.setAimAtGoalDuringFinalLeg(true);
        var swerveToOtherEdgeWhileStrafing = swerveProvider.get();

        setUpLogic(swerveToOtherEdgeWhileStrafing,2);

        //scooches first note, then reverse scooches the rest
        this.addCommands(Commands.deadline(swerveToEdge,scoochIntake,setArmToScooch));
        this.addCommands(Commands.deadline(swerveToOtherEdgeWhileStrafing,scoochEject));

        //sets arm and collects Centerline 5 note
        var swerveLastNote = swerveProvider.get();
        setUpLogic(swerveLastNote,3);
        swerveLastNote.logic.setAimAtGoalDuringFinalLeg(true);
        swerveLastNote.logic.setDriveBackwards(true);

        var setArmToCollect = setArmAngleProvider.get();
        setArmToCollect.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        //swap the swerve and the intake later this is just for simulator
        this.addCommands(setArmToCollect.alongWith(Commands.deadline(swerveLastNote,intakeUntilCollected)));

        var moveNoteAwayFromShooterWheels =
                eject.withTimeout(0.1).andThen(stopCollector.withTimeout(0.05));

        //drives back to the subwoofer and scores
        var driveToSubwoofer = swerveProvider.get();
        setUpLogic(driveToSubwoofer,488);

        var warmUpForSecondSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForSecondSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        var fireSecondShot = fireWhenReadyCommandProvider.get();

        this.addCommands(Commands.deadline(driveToSubwoofer,
                moveNoteAwayFromShooterWheels.andThen(warmUpForSecondSubwooferShot)));
        this.addCommands(fireSecondShot);

    }
    //sets up basic logic when given a swerve command
    private void setUpLogic(SwerveSimpleTrajectoryCommand swerve,double key){
        swerve.logic.setEnableConstantVelocity(true);
        swerve.logic.setConstantVelocity(4.5);
        swerve.logic.setKeyPoints(getPoints(key));
        swerve.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
    }
    //a key to make this function return different points based on what you need
    private ArrayList<XbotSwervePoint> getPoints(double key){
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        //drives to the first centerline note with an angle ready to collect
        if (key == 1) {
            //points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.45,7.95), Rotation2d.fromDegrees(235), 10));
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine1, 10));
            return points;
        }
        //drives through 4 centerline notes
        if(key == 2){
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.45,2.388), Rotation2d.fromDegrees(65),10));
            return points;
        }
        //drives back to subwoofer
        if(key == 3){
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.296,0.6),new Rotation2d(),10));
            return points;
        }
        //any other key just sets it to this
        var target = BasePoseSubsystem.convertBlueToRedIfNeeded(new Translation2d(15.2,5.553));
        points.add(new XbotSwervePoint(target,Rotation2d.fromDegrees(0), 10));
        return points;
    }
}
