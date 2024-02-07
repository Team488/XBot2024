package competition.commandgroups;

import competition.subsystems.arm.commands.WaitForArmToBeAtGoalCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Array;
import java.util.ArrayList;

public class DriveToNoteAndIntake extends ParallelCommandGroup {

    @Inject
    DriveToNoteAndIntake(Provider<SwerveSimpleTrajectoryCommand> swerveProvider,IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand,WaitForArmToBeAtGoalCommand waitForArm){
        SwerveSimpleTrajectoryCommand swerveToNote = swerveProvider.get();
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.CenterLine5,10));
        swerveToNote.logic.setKeyPoints(swervePoints);
        swerveToNote.logic.setEnableConstantVelocity(true);
        swerveToNote.logic.setConstantVelocity(1);

        this.addCommands(waitForArm);
        this.addCommands(swerveToNote);
        this.addCommands(intakeUntilNoteCollectedCommand);




    }
}
