package competition.commandgroups;

import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class DriveToGivenNoteAndCollectCommandGroup extends ParallelDeadlineGroup {


    @Inject
    public DriveToGivenNoteAndCollectCommandGroup(DriveToGivenNoteCommand driveToGivenNoteCommand,
                                                  CollectSequenceCommandGroup collectSequenceCommandGroup,
                                                  WaitForNoteCollectedCommand waitForNoteCollectedCommand) {
        super(waitForNoteCollectedCommand);

        this.addCommands(driveToGivenNoteCommand, collectSequenceCommandGroup);

        // use this for testing in simulator
//        super(driveToGivenNoteCommand);
//
//        this.addCommands(collectSequenceCommandGroup);
    }
}