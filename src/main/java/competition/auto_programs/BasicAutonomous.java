package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class BasicAutonomous extends SequentialCommandGroup {

    @Inject
    BasicAutonomous(FireNoteCommandGroup fireNoteCommand) {;

        // Set up arm angle (for point blank shot)
        // Spin up shooter (for ^^^)
        // Fire when ready
        // Wait some time for note to clear robot
        // Drive to a position

        // Fire
        this.addCommands(fireNoteCommand);

        // Drive to position (I don't think we have any swerve to point yet)
        // this.addCommands();
    }
}
