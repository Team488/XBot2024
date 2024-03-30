package competition.commandgroups;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class DriveToCentralSubwooferAndFireIfHasNoteCommandGroup extends SequentialCommandGroup {

    CollectorSubsystem collector;

    @Inject
    public DriveToCentralSubwooferAndFireIfHasNoteCommandGroup(DriveToCentralSubwooferAndFireCommandGroup
                                                                           driveToCentralSubwooferAndFireCommandGroup) {
        // Since this is only one command, I think we can probably simplify it to not be a command group?
        var driveAndFireIfNote = new ConditionalCommand(
                driveToCentralSubwooferAndFireCommandGroup,
                new InstantCommand(),
                this::getContainsNote
        );
        this.addCommands(driveAndFireIfNote);
    }

    private boolean getContainsNote() {
        return collector.getBeamBreakSensorActivated() || collector.getGamePieceInControl()
                || collector.getGamePieceReady();
    }
}
