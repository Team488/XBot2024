package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.arm.commands.WaitForArmToBeAtGoalCommand;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Array;
import java.util.ArrayList;
import java.util.stream.Collector;

public class DriveToNoteAndIntake extends ParallelDeadlineGroup {
    Pose2d notePosition = PoseSubsystem.CenterLine1;
    @Inject
    DriveToNoteAndIntake(
            Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
            IntakeUntilNoteCollectedCommand intakeUntilNoteCollected,
            Provider<WaitForArmToBeAtGoalCommand> waitForArmProvider,
            Provider<SetArmAngleCommand> setArmAngleProvider,
            WaitForNoteCollectedCommand waitForNote)
    {
        //sets the deadline to waitForNoteCollected
        super(waitForNote);
        setDeadline(waitForNote);

        SwerveSimpleTrajectoryCommand swerveToNote = swerveProvider.get();
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();

        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                notePosition,10));
        //pretty sure swerveToNote faces the shooter towards the note
        //so by setting drive to backwards it will face the collector towards the note (Since the collector is on the back)
        swerveToNote.logic.setKeyPoints(swervePoints);
        swerveToNote.logic.setDriveBackwards(true);
        swerveToNote.logic.setEnableConstantVelocity(true);
        swerveToNote.logic.setConstantVelocity(1);

        SetArmAngleCommand extendArm = setArmAngleProvider.get();
        extendArm.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        var extendThenSwerveToNoteInCorrectOrientation = new SequentialCommandGroup(extendArm,waitForArmProvider.get(),swerveToNote);
        //extends arm then drives to note
        this.addCommands(extendThenSwerveToNoteInCorrectOrientation);
        // runs the intake in parallel
        this.addCommands(intakeUntilNoteCollected);

        SetArmAngleCommand retractArm = setArmAngleProvider.get();
        retractArm.setArmPosition(ArmSubsystem.UsefulArmPosition.STARTING_POSITION);

        //waits for arm to be in position then intakes and retracts
        var retract = new SequentialCommandGroup(retractArm, waitForArmProvider.get());
        this.addCommands(retract);

    }
    public void setNotePosition(Pose2d notePosition){
        this.notePosition = notePosition;
    }
}
