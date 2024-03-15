package competition.commandgroups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.oracle.NoteMap;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class CollectNearestNoteUsingVisionCommandGroup extends SequentialCommandGroup {

    DynamicOracle oracle;
    PoseSubsystem pose;
    NoteMap noteMap;

    @Inject
    CollectNearestNoteUsingVisionCommandGroup(VisionSubsystem vision, PoseSubsystem pose,
                                              SwerveSimpleTrajectoryCommand swerveSimpleTrajectoryCommand,
                                              Provider<DriveToGivenNoteAndCollectCommandGroup> driveAndCollectProvider,
                                              DriveSubsystem drive, DynamicOracle oracle) {

        this.oracle = oracle;
        this.pose = pose;
        this.noteMap = oracle.getNoteMap();

        // We are assuming that our position and everything else has already been initialized by the auto

        // Obtain notes position data
        // vision.periodic(); // Update detectedNotes?
        //Pose3d[] unprocessedNotePositions = vision.getDetectedNotes();
        //Pose2d[] notePositions = new Pose2d[unprocessedNotePositions.length];

        // Potential problem: what if our notePositions are empty?
        /*if (unprocessedNotePositions.length <= 0) {
            return;
        }*/

        // Process all our Pose3d to 2d
//        for (int i = 0; i < unprocessedNotePositions.length; i++) {
//            notePositions[i] = unprocessedNotePositions[i].toPose2d();
//        }

        // Find closest note
        // Get our currentPosition
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);
//        Pose2d currentPosition = pose.getVisionAssistedPositionInMeters();
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
        //final Pose2d finalPosition = closestNote;


//        this.addCommands(
//                new InstantCommand(() -> {
//                    drive.setTargetNote(getClosestNote());
//                })
//        );
//        var driveToCollectNote = driveAndCollectProvider.get();
//        this.addCommands(driveToCollectNote);
        var goToNote = pose.createSetPositionCommand(getClosestNote());
        this.addCommands(goToNote);
//        ArrayList<XbotSwervePoint> points = new ArrayList<>();
//        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(getClosestNote(),10));
//        swerveSimpleTrajectoryCommand.logic.setEnableConstantVelocity(true);
//        swerveSimpleTrajectoryCommand.logic.setConstantVelocity(4.5);
//        swerveSimpleTrajectoryCommand.logic.setKeyPoints(points);
   }
   private Pose2d getClosestNote(){
        // DynamicOracle is not meant to work, we have to put mock code to fake it
        // (Said by Alex the mentor)

        var noteLocation = noteMap.getClosestAvailableNote(PoseSubsystem.convertBluetoRed(PoseSubsystem.BlueSubwooferTopScoringLocation));
            return noteLocation.toPose2d();
//       var noteLocation = noteMap.getClosestAvailableNote(
//               PoseSubsystem.convertBlueToRedIfNeeded(pose.getCurrentPose2d())).toPose2d();
//       return noteLocation;
   }
}
