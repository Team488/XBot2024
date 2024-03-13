package competition.commandgroups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;

import javax.inject.Inject;

public class CollectNearestNoteUsingVisionCommandGroup extends SequentialCommandGroup {

    @Inject
    CollectNearestNoteUsingVisionCommandGroup(VisionSubsystem vision, PoseSubsystem pose,
                                              SwerveSimpleTrajectoryCommand swerveSimpleTrajectoryCommand,
                                              DriveToGivenNoteAndCollectCommandGroup driveAndCollect, DriveSubsystem drive,
                                              DynamicOracle oracle) {

        // We are assuming that our position and everything else has already been initialized by the auto

        // Obtain notes position data
        // vision.periodic(); // Update detectedNotes?
        Pose3d[] unprocessedNotePositions = vision.getDetectedNotes();
        Pose2d[] notePositions = new Pose2d[unprocessedNotePositions.length];

        // Potential problem: what if our notePositions are empty?
        /*if (unprocessedNotePositions.length <= 0) {
            return;
        }*/

        // Process all our Pose3d to 2d
        for (int i = 0; i < unprocessedNotePositions.length; i++) {
            notePositions[i] = unprocessedNotePositions[i].toPose2d();
        }

        // Find closest note
        // Get our currentPosition
        Pose2d currentPosition = pose.getVisionAssistedPositionInMeters();

        Pose2d closestNote = oracle.getNoteMap().getClosestAvailableNote(currentPosition).toPose2d();

        /*
        Pose2d nearestNotePosition = notePositions[0];
        double leastDistance = PoseSubsystem.convertBlueToRedIfNeeded(
                notePositions[0]).getTranslation().getDistance(currentPosition.getTranslation());

        // Get closest note
        for (int i = 1; i < notePositions.length; i++) {
            double distance =  PoseSubsystem.convertBlueToRedIfNeeded(
                    notePositions[i]).getTranslation().getDistance(currentPosition.getTranslation());

            if (distance < leastDistance) {
                nearestNotePosition = notePositions[i];
                leastDistance = distance;
            }
        }*/

        // Drive and also intake
        //final Pose2d finalNearestNotePosition = nearestNotePosition;
        final Pose2d finalPosition = closestNote;
        this.addCommands(
                new InstantCommand(() -> {
                    drive.setTargetNote(closestNote);
                })
        );
        this.addCommands(driveAndCollect);
    }
}
