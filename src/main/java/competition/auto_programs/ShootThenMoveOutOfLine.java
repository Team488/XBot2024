package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class ShootThenMoveOutOfLine extends SequentialCommandGroup {

    @Inject
    ShootThenMoveOutOfLine(FireNoteCommandGroup fireNoteCommand,
                           SwerveSimpleTrajectoryCommand moveOutOfLineCommand,
                           PoseSubsystem pose) {
        // Fire the note we hold at the start
        this.addCommands(fireNoteCommand);

        // Move straight out of line
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Pose2d(2.65, pose.getCurrentVelocity().y, new Rotation2d()), 10));
        moveOutOfLineCommand.logic.setKeyPoints(points);
        moveOutOfLineCommand.logic.setEnableConstantVelocity(true);
        moveOutOfLineCommand.logic.setConstantVelocity(1);

        this.addCommands(moveOutOfLineCommand);
    }
}
