package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
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


public class TwoNoteGriefAuto extends SequentialCommandGroup {
    DynamicOracle oracle;
    @Inject
    public TwoNoteGriefAuto(Provider<FireNoteCommandGroup> fireNoteProvider,
                            Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
                            Provider<SetArmAngleCommand> setArmAngleProvider,
                            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
                            Provider<FireWhenReadyCommand> fireWhenReadyCommandProvider,
                            IntakeScoocherCommand scoochIntake,
                            IntakeUntilNoteCollectedCommand intakeUntilCollected,
                            EjectCollectorCommand eject,
                            StopCollectorCommand stopCollector,
                            PoseSubsystem pose,
                            DynamicOracle oracle){
        this.oracle = oracle;

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SubwooferCentralScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

//        var fireFirstNote = fireNoteProvider.get();
//        this.addCommands(Commands.deadline(fireFirstNote));
        var warmUpForFirstSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForFirstSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        var fireFirstShot = fireWhenReadyCommandProvider.get();

        this.addCommands(Commands.deadline(fireFirstShot,
                warmUpForFirstSubwooferShot));

        var setArmToScooch = setArmAngleProvider.get();
        setArmToScooch.setArmPosition(ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE);
        var swerveToEdge = swerveProvider.get();
        setUpLogic(swerveToEdge,1);
        swerveToEdge.logic.setAimAtIntermediateNonFinalLegs(true);

        this.addCommands(Commands.deadline(swerveToEdge,scoochIntake,setArmToScooch));

        var swerveLastNote = swerveProvider.get();
        setUpLogic(swerveLastNote,2);
        swerveLastNote.logic.setDriveBackwards(true);

        var setArmToCollect = setArmAngleProvider.get();
        setArmToCollect.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        //swap the swerve and the intake later this is just for simulator
        this.addCommands(setArmToCollect.alongWith(Commands.deadline(swerveLastNote,intakeUntilCollected)));

        var moveNoteAwayFromShooterWheels =
                eject.withTimeout(0.1).andThen(stopCollector.withTimeout(0.05));

        var driveToSubwoofer = swerveProvider.get();
        setUpLogic(driveToSubwoofer,488);

        var warmUpForSecondSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForSecondSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);
        var fireSecondShot = fireWhenReadyCommandProvider.get();

        this.addCommands(Commands.deadline(driveToSubwoofer,
                moveNoteAwayFromShooterWheels.andThen(warmUpForSecondSubwooferShot)));
        this.addCommands(fireSecondShot);

    }
    private void setUpLogic(SwerveSimpleTrajectoryCommand swerve,double key){
        swerve.logic.setAimAtGoalDuringFinalLeg(true);
        //swerve.logic.setDriveBackwards(true);
        swerve.logic.setEnableConstantVelocity(true);
        swerve.logic.setConstantVelocity(4.5);
        swerve.logic.setKeyPoints(getPoints(key));
        swerve.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());
    }
    //a key to make this function return different points based on what you need
    private ArrayList<XbotSwervePoint> getPoints(double key){
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        //drive through center line notes
        if (key == 1) {
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.509,7.7), Rotation2d.fromDegrees(240), 10));
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.509,0.6), Rotation2d.fromDegrees(240),10));
            return points;
        }
        //drive through Centerline5 note
        if(key == 2){
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.296,0.6),new Rotation2d(),10));
            return points;
        }
        //points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(2.9,3.8),new Rotation2d(),10));
        //any other key just sets it to this
        var target = BasePoseSubsystem.convertBlueToRedIfNeeded(new Translation2d(14.7,5.553));
        //points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(14.224,2.429),Rotation2d.fromDegrees(0),10));
        points.add(new XbotSwervePoint(target,Rotation2d.fromDegrees(180), 10));
        return points;
    }
}
