package competition.auto;

import competition.commandgroups.FireNoteCommandGroup;
import competition.commandgroups.PrepareToFireAtSpeakerCommandGroup;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class MidNoteCommandGroup extends SequentialCommandGroup {
    @Inject
    public MidNoteCommandGroup(MidNoteCommand midNoteCommand,
                               Provider<FireNoteCommandGroup> fireNoteCommandGroupProvider,
                               Provider<PrepareEverywhereCommandGroup> prepareEverywhereCommandGroupProvider,
                               Provider<WarmUpShooterCommand> warmUpShooterCommandProvider) {

        var warmUpPreload = warmUpShooterCommandProvider.get();
        warmUpPreload.setTargetRpm(ShooterWheelSubsystem.TargetRPM.SUBWOOFER);

        this.addCommands(warmUpPreload);
        var firePreload = fireNoteCommandGroupProvider.get();
        this.addCommands(firePreload);

        this.addCommands(midNoteCommand);

        var prepareSpikeNote = prepareEverywhereCommandGroupProvider.get();
        this.addCommands(prepareSpikeNote);
        var fireFromSpike = fireNoteCommandGroupProvider.get();
        this.addCommands(fireFromSpike);
    }
}
