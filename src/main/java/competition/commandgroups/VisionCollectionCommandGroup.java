package competition.commandgroups;

import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;

import javax.inject.Inject;

public class VisionCollectionCommandGroup extends SequentialCommandGroup {

    @Inject
    VisionCollectionCommandGroup(VisionSubsystem vision, PoseSubsystem pose,
                                 SwerveSimpleTrajectoryCommand swerveSimpleTrajectoryCommand,
                                 DriveToGivenNoteAndCollectCommandGroup driveAndCollect) {

        // We are assuming that our position and everything else has already been initialized by the auto

        // Obtain notes position data
        // vision.periodic(); // Update detectedNotes?
        Pose3d[] unprocessedNotePositions = vision.getDetectedNotes();
        Pose2d[] notePositions = new Pose2d[unprocessedNotePositions.length];

        // Process all our Pose3d to 2d
        for (int i = 0; i < unprocessedNotePositions.length; i++) {
            notePositions[i] = unprocessedNotePositions[i].toPose2d();
        }

        // Find closest note
        // Get our currentPosition
        Pose2d currentPosition = pose.getVisionAssistedPositionInMeters();
        Pose2d nearestNote;

        

        // Drive and also intake
    }
}
