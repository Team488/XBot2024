package competition.auto_pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

import javax.inject.Inject;
import javax.inject.Provider;

public class AutoFactory {
    PoseSubsystem pose;
    DriveSubsystem driveSubsystem;
    PathPlannerDriveSubsystem drive;

    @Inject
    public AutoFactory(DriveSubsystem driveSubsystem,
                       PathPlannerDriveSubsystem drive,
                       PoseSubsystem pose) {
        this.pose = pose;
        this.drive = drive;
        this.driveSubsystem = driveSubsystem;
    }

    public Command createBNB(Provider<IntakeCollectorCommand> collectNote) {
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));

        SequentialCommandGroup c = new SequentialCommandGroup();
        c.addCommands(startInFrontOfSpeaker);

        c.addCommands(followWhileIntaking(Location.SUBWOOFER_MID, Location.SPIKE_MID, collectNote));
        c.addCommands(new WaitCommand(2));
        c.addCommands(follow(Location.SPIKE_MID, Location.SUBWOOFER_MID));
        c.addCommands(new WaitCommand(2));

        c.addCommands(followWhileIntaking(Location.SUBWOOFER_MID, Location.SPIKE_TOP, collectNote));
        c.addCommands(new WaitCommand(2));

        c.addCommands(follow(Location.SPIKE_TOP, Location.SUBWOOFER_MID));
        c.addCommands(new WaitCommand(2));

        c.addCommands(followWhileIntaking(Location.SUBWOOFER_MID, Location.SPIKE_BOT, collectNote));
        c.addCommands(new WaitCommand(2));

        c.addCommands(follow(Location.SPIKE_BOT, Location.SUBWOOFER_MID));

        return c;
    }

    public Command testing() {
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(
                        new Pose2d(new Translation2d(2.75, 6.89), Rotation2d.fromDegrees(-134.68))));

        SequentialCommandGroup c = new SequentialCommandGroup();
        c.addCommands(startInFrontOfSpeaker);

        var move = follow(Location.SPIKE_TOP, Location.SUBWOOFER_MID);
        c.addCommands(move);
        return c;
    }

    ParallelDeadlineGroup followWhileIntaking(Location start, Location end,
                                              Provider<IntakeCollectorCommand> intakeCollectorCommandProvider) {
        var intake = intakeCollectorCommandProvider.get();
        return follow(start, end).deadlineWith(intake);
    }

    Command follow(Location start, Location end) {
        var name = "%S_TO_%S".formatted(start, end);
//        var path =  AutoBuilder.followPath(PathPlannerPath.fromPathFile(name));
        var auto =  AutoBuilder.followPath(PathPlannerPath.fromPathFile(name));


//        return new BasePathPlannerCommand(drive, driveSubsystem, path);
//        return new FollowPathCommand(drive, driveSubsystem, pose, PathPlannerPath.fromPathFile(name));

        var command = new FollowPathCommand(driveSubsystem, pose);
        command.setPath(PathPlannerPath.fromPathFile(name));
        return command;
    }

    Pose2d getStartingPose(Location start, Location end) {
        var name = "%S_TO_%S:".formatted(start, end);
        return PathPlannerPath.fromPathFile(name).getPreviewStartingHolonomicPose();
    }
}