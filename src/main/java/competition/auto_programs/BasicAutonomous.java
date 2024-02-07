package competition.auto_programs;

import competition.commandgroups.FireNoteCommand;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class BasicAutonomous extends SequentialCommandGroup {

    @Inject
    BasicAutonomous(FireNoteCommand fireNoteCommand) {;
        // Set up arm angle (for point blank shot)
        // Spin up shooter (for ^^^)
        // Fire when ready
        // Wait some time for note to clear robot
        // Drive to a position

        // Fire
        FireNoteCommand
    }
}
