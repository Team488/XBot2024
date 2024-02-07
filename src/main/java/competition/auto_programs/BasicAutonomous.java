package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class BasicAutonomous extends SequentialCommandGroup {

    @Inject
    BasicAutonomous(FireNoteCommandGroup fireNoteCommand, SwerveSimpleTrajectoryCommand swerveSimpleTrajectoryCommand) {

        // Set up arm angle (for point blank shot)
        // Spin up shooter (for ^^^)
        // Fire when ready
        // Wait some time for note to clear robot
        // Drive to a position

        // Fire
        this.addCommands(fireNoteCommand);

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                PoseSubsystem.CenterLine1, 10));
        swerveSimpleTrajectoryCommand.logic.setKeyPoints(points);
        swerveSimpleTrajectoryCommand.logic.setEnableConstantVelocity(true);
        swerveSimpleTrajectoryCommand.logic.setConstantVelocity(1);
        this.addCommands(swerveSimpleTrajectoryCommand);
    }
}
