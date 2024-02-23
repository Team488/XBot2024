package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.IntakeScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
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


//we gonna get opps pulling this out mid competition😶‍🌫️🥶😰😰
public class TwoNoteGriefAuto extends SequentialCommandGroup {
    @Inject
    public TwoNoteGriefAuto(Provider<FireNoteCommandGroup> fireNoteProvider,
                            Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
                            Provider<SetArmAngleCommand> setArmAngleProvider,
                            Provider<WarmUpShooterCommand> warmUpShooterCommandProvider,
                            IntakeScoocherCommand scoochCheese,
                            IntakeUntilNoteCollectedCommand intakeUntilCollected,
                            EjectCollectorCommand eject,
                            StopCollectorCommand stopCollector,
                            PoseSubsystem pose){
        InstantCommand forceSetPosition = new InstantCommand(
                () -> {
                    pose.setCurrentPoseInMeters(
                            BasePoseSubsystem.convertBlueToRedIfNeeded(new Pose2d(3.785, 6.2, new Rotation2d()))
                    );
                }
        );
        //sets our position near the top right of our wing
        this.addCommands(forceSetPosition);
        this.addCommands(fireNoteProvider.get());

        var setArmToScooch = setArmAngleProvider.get();
        setArmToScooch.setArmPosition(ArmSubsystem.UsefulArmPosition.SCOOCH_NOTE);
        var swerveToEdge = swerveProvider.get();
        setUpLogic(swerveToEdge,1);

        //THEY CAN MAKE A TEN NOTE AUTO AND ILL STILL DISABLE THEIR SHII 😂😪😴😛
        var antiJackInTheBot = new ParallelCommandGroup(swerveToEdge, setArmToScooch, scoochCheese);
        this.addCommands(antiJackInTheBot);
        //🤫🧏‍♂

        var swerveLastNote = swerveProvider.get();
        setUpLogic(swerveLastNote,2);
        swerveLastNote.logic.setDriveBackwards(true);
        var setArmToCollect = setArmAngleProvider.get();
        setArmToCollect.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        this.addCommands(setArmToCollect.andThen(Commands.deadline(intakeUntilCollected,swerveLastNote)
                .andThen(eject.withTimeout(0.1))
                .andThen(stopCollector.withTimeout(0.05))));

        var driveToSubwoofer = swerveProvider.get();
        setUpLogic(driveToSubwoofer,488);

        var warmUpForSecondSubwooferShot = warmUpShooterCommandProvider.get();
        warmUpForSecondSubwooferShot.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);

        this.addCommands(Commands.deadline(driveToSubwoofer,warmUpForSecondSubwooferShot).andThen(fireNoteProvider.get()));
    }
    private void setUpLogic(SwerveSimpleTrajectoryCommand swerve,double key){
        swerve.logic.setAimAtGoalDuringFinalLeg(true);
        //swerve.logic.setDriveBackwards(true);
        swerve.logic.setEnableConstantVelocity(true);
        swerve.logic.setConstantVelocity(1);
        swerve.logic.setKeyPoints(getPoints(key));
    }
    //a key to make this function return different points based on what you need
    private ArrayList<XbotSwervePoint> getPoints(double key){
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        //drive through center line notes
        if (key == 1) {
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine1, 10));
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine2, 10));
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine3, 10));
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine4, 10));
            //points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine5,10));
            return points;
        }
        //drive through Centerline5 note
        if(key == 2){
            points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(8.296,0.5),new Rotation2d(),10));
            return points;
        }
        //points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(new Translation2d(2.9,3.8),new Rotation2d(),10));
        //any other key just sets it to this
        var target = BasePoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SubwooferCentralScoringLocation);
        points.add(new XbotSwervePoint(target.getTranslation(), target.getRotation(), 10));
        return points;
    }
}
