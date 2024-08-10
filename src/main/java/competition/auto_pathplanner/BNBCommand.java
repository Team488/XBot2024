package competition.auto_pathplanner;

import com.pathplanner.lib.path.PathPlannerPath;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
 import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;
import javax.inject.Provider;

public class BNBCommand extends SequentialCommandGroup {
    CollectorSubsystem collectorSubsystem;

    @Inject
    public BNBCommand(Provider<IntakeCollectorCommand> intakeCollectorCommandProvider, PoseSubsystem pose,
                      Provider<FollowPathCommand> followPathCommandProvider,
                      CollectorSubsystem collectorSubsystem) {

        this.collectorSubsystem = collectorSubsystem;

        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        var intakeSpikeMid = intakeCollectorCommandProvider.get();
        var goSpikeMid = followPathCommandProvider.get();
        goSpikeMid.setPath(PathPlannerPath.fromPathFile("SUBWOOFER_MID_TO_SPIKE_MID"));
        this.addCommands(goSpikeMid.deadlineWith(intakeSpikeMid));

        this.addCommands(
                new InstantCommand(() -> {
                    collectorSubsystem.stop();
                })
        );

        this.addCommands(new WaitCommand(1));

        var spikeMidBack = followPathCommandProvider.get();
        spikeMidBack.setPath(PathPlannerPath.fromPathFile("SPIKE_MID_TO_SUBWOOFER_MID"));
        this.addCommands(spikeMidBack);

        //SHOOT HERE
        this.addCommands(new WaitCommand(2));



        var intakeSpikeTop = intakeCollectorCommandProvider.get();
        var goSpikeTop = followPathCommandProvider.get();
        goSpikeTop.setPath(PathPlannerPath.fromPathFile("SUBWOOFER_MID_TO_SPIKE_TOP"));
        this.addCommands(goSpikeTop.deadlineWith(intakeSpikeTop));

        this.addCommands(
                new InstantCommand(() -> {
                    collectorSubsystem.stop();
                })
        );

        this.addCommands(new WaitCommand(1));

        var spikeTopBack = followPathCommandProvider.get();
        spikeTopBack.setPath(PathPlannerPath.fromPathFile("SPIKE_TOP_TO_SUBWOOFER_MID"));
        this.addCommands(spikeTopBack);

        //SHOOT HERE
        this.addCommands(new WaitCommand(2));


        var intakeSpikeBot = intakeCollectorCommandProvider.get();
        var goSpikeBot = followPathCommandProvider.get();
        goSpikeBot.setPath(PathPlannerPath.fromPathFile("SUBWOOFER_MID_TO_SPIKE_BOT"));
        this.addCommands(goSpikeBot.deadlineWith(intakeSpikeBot));

        this.addCommands(
                new InstantCommand(() -> {
                    collectorSubsystem.stop();
                })
        );

        this.addCommands(new WaitCommand(1));

        var spikeBotBack = followPathCommandProvider.get();
        spikeBotBack.setPath(PathPlannerPath.fromPathFile("SPIKE_BOT_TO_SUBWOOFER_MID"));
        this.addCommands(spikeBotBack);

    }
}
