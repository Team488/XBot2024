package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.arm.commands.WaitForArmToBeAtGoalCommand;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Array;
import java.util.ArrayList;
import java.util.stream.Collector;

public class DriveToNoteAndIntake extends SequentialCommandGroup {
    @Inject
    DriveToNoteAndIntake(Provider<SwerveSimpleTrajectoryCommand> swerveProvider, Pose2d notePosition, IntakeUntilNoteCollectedCommand intakeUntilNoteCollected, Provider<WaitForArmToBeAtGoalCommand> waitForArmProvider, Provider<SetArmAngleCommand> setArmAngleProvider){

        SwerveSimpleTrajectoryCommand swerveToNote = swerveProvider.get();
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();

        //doesnt set an angle though im pretty sure? unless pose2d can calculate the angle towards the note
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                notePosition,10));
        swerveToNote.logic.setKeyPoints(swervePoints);
        swerveToNote.logic.setEnableConstantVelocity(true);
        swerveToNote.logic.setConstantVelocity(1);

        SetArmAngleCommand extendArm = setArmAngleProvider.get();
        extendArm.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        //drives to note then extends the arm
        var swerveToNoteAndExtend = new ParallelCommandGroup(swerveToNote,extendArm);

        //waits for arm in case there are any previous commands called that use the arm
        this.addCommands(waitForArmProvider.get());
        this.addCommands(swerveToNoteAndExtend);

        SetArmAngleCommand retractArm = setArmAngleProvider.get();
        retractArm.setArmPosition(ArmSubsystem.UsefulArmPosition.STARTING_POSITION);
        //waits for arm to be in position then intakes and retracts
        var intakeAndRetract = new SequentialCommandGroup(waitForArmProvider.get(),intakeUntilNoteCollected,retractArm, waitForArmProvider.get());

        this.addCommands(intakeAndRetract);

    }
}
