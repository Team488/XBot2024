package competition.commandgroups;

import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class DriveToGivenNoteAndCollectCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public DriveToGivenNoteAndCollectCommandGroup(IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand,
                                                  DriveToGivenNoteCommand driveToGivenNoteCommand,
                                                  CollectSequenceCommandGroup collectSequenceCommandGroup) {
        super(intakeUntilNoteCollectedCommand);

        this.addCommands(driveToGivenNoteCommand, collectSequenceCommandGroup);
    }
}