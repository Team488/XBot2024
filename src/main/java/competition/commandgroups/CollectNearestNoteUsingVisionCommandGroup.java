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


        /* Delete at the VERY end once we know for sure that everything else is for sure working.
        // Set our starting position to be at red top subwoofer
        var startAtTopOfSubwoofer = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferTopScoringLocation)
        );
        this.addCommands(startAtTopOfSubwoofer);

        // Go to the closest position
        var goToClosest = pose.createSetPositionCommand(
                this::getClosestNote
        );
        this.addCommands(goToClosest);
        */
    }

    private Pose2d getClosestNote() {
        var noteLocation = noteMap.getClosestAvailableNote(
                PoseSubsystem.convertBlueToRedIfNeeded(pose.getCurrentPose2d()), true);
        return noteLocation.toPose2d();
    }
}