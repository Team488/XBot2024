package competition.commandgroups;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class DriveToCentralSubwooferAndFireIfHasNoteCommandGroup extends SequentialCommandGroup {

    // Kinda ugly, have to hard code interstageTimeout because we can't
    // get the value from our brd&btr command group
    // because then it's gonna be importing each other
    // any way to solve this? I feel like I run into this type of problem a lot
    double interstageTimeout = 3.5;
    CollectorSubsystem collector;

    @Inject
    public DriveToCentralSubwooferAndFireIfHasNoteCommandGroup(FireFromSubwooferCommandGroup fireFromSubwooferCommandGroup,
                                                               DriveToCentralSubwooferCommand driveToCentralSubwooferCommand) {
        var driveBackToSubwooferIfHasTopSpike = new ConditionalCommand(
                driveToCentralSubwooferCommand.withTimeout(interstageTimeout),
                new InstantCommand(),
                this::getContainsNote
        );
        var fireNoteIfHasNote = new ConditionalCommand(
                fireFromSubwooferCommandGroup,
                new InstantCommand(),
                this::getContainsNote
        );
        this.addCommands(driveBackToSubwooferIfHasTopSpike, fireNoteIfHasNote);
    }

    private boolean getContainsNote() {
        return collector.confidentlyHasControlOfNote();
    }
}
