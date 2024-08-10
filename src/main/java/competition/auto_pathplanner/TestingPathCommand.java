package competition.auto_pathplanner;

import com.pathplanner.lib.path.PathPlannerPath;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;
import javax.inject.Provider;

public class TestingPathCommand extends SequentialCommandGroup {
    CollectorSubsystem collectorSubsystem;

    @Inject
    public TestingPathCommand(Provider<IntakeCollectorCommand> intakeCollectorCommandProvider, PoseSubsystem pose,
                              Provider<FollowPathCommand> followPathCommandProvider,
                              Provider<StopCollectorCommand> stopCollectorCommandProvider, CollectorSubsystem collectorSubsystem) {

        this.collectorSubsystem = collectorSubsystem;

        var startingPose = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(
                        new Pose2d(new Translation2d(2.4, 6.62), new Rotation2d(Math.toRadians(180)))));
        this.addCommands(startingPose);

        var intake = intakeCollectorCommandProvider.get();
        var path1 = followPathCommandProvider.get();
        path1.setPath(PathPlannerPath.fromPathFile("TestX"));

        this.addCommands(path1.deadlineWith(intake));

        var stopIntake = stopCollectorCommandProvider.get();

        this.addCommands(new WaitCommand(2));

//        this.addCommands(stopIntake);

        this.addCommands(
                new InstantCommand(() -> {
                    collectorSubsystem.stop();
                })
        );

        var path2 = followPathCommandProvider.get();
        path2.setPath(PathPlannerPath.fromPathFile("TestXY"));

        this.addCommands(path2);

    }
}
