package competition.auto_pathplanner;

import com.pathplanner.lib.path.PathPlannerPath;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
 import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;
import javax.inject.Provider;

public class BNBCommand extends SequentialCommandGroup {

    @Inject
    public BNBCommand(AutoFactory autoFactory,
                      Provider<IntakeCollectorCommand> intakeCollectorCommandProvider, PoseSubsystem pose) {

        this.addCommands(new Command() {
            @Override
            public void initialize() {
                System.out.println("Print Command Initialized.");
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        });
        var startInFrontOfSpeaker = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferMiddleScoringLocation));
        this.addCommands(startInFrontOfSpeaker);

        var intakeSpikeMid = intakeCollectorCommandProvider.get();

        var spikeMidBack = autoFactory.follow(Location.SPIKE_MID, Location.SUBWOOFER_MID);

        this.addCommands(spikeMidBack.deadlineWith(intakeSpikeMid));




    }
}
