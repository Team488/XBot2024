package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class ShootThenMoveOutOfLine extends SequentialCommandGroup {

    @Inject
    ShootThenMoveOutOfLine(FireNoteCommandGroup fireNoteCommand,
                           SwerveSimpleTrajectoryCommand moveOutOfLineCommand,
                           PoseSubsystem pose) {

        // Force set our position first.
        // This is our starting position, to be changed later if needed.
        // Right now we are assuming that we are staring right in front of the speaker.
        InstantCommand forceSetPosition = new InstantCommand(
                () -> {
                    pose.setCurrentPoseInMeters(
                            BasePoseSubsystem.convertBlueToRedIfNeeded(new Pose2d(0.5, 5.5478, new Rotation2d()))
                    );
                }
        );
        this.addCommands(forceSetPosition);

        // Fire the note we hold at the start
        this.addCommands(fireNoteCommand);

        // Move in the positive x direction, in theory this should get us from our starting point
        // To outside of the line (for points), and then stop right in front of the note
        moveOutOfLineCommand.logic.setKeyPointsProvider(this::createPointsToMoveOutOfLine);
        moveOutOfLineCommand.logic.setEnableConstantVelocity(true);
        moveOutOfLineCommand.logic.setConstantVelocity(1);


        this.addCommands(moveOutOfLineCommand);
    }

    private ArrayList<XbotSwervePoint> createPointsToMoveOutOfLine() {
        // Returns a list with a point that is outside of line, in front of a note
        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                new Pose2d(2.65, 5.5478, new Rotation2d()), 10));
        return points;
    }
}
