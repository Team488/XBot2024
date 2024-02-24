package competition.auto;

import competition.subsystems.arm.commands.ContinuouslyPointArmAtSpeakerCommand;
import competition.subsystems.shooter.commands.ContinuouslyWarmUpForSpeakerCommand;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class PrepareEverywhereCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public PrepareEverywhereCommandGroup(ContinuouslyWarmUpForSpeakerCommand continuouslyWarmUpForSpeakerCommand,
                                         ContinuouslyPointArmAtSpeakerCommand continuouslyPointArmAtSpeakerCommand,
                                         FireWhenReadyCommand fireWhenReadyCommand) {
        super(fireWhenReadyCommand);
        this.addCommands(continuouslyWarmUpForSpeakerCommand, continuouslyPointArmAtSpeakerCommand);
    }

}
