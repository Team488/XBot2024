package competition.commandgroups;

import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class DriveToGivenNoteAndCollectCommandGroup extends ParallelDeadlineGroup {


    @Inject
    public DriveToGivenNoteAndCollectCommandGroup(DriveToGivenNoteWithVisionCommand driveToGivenNoteCommand,
                                                  CollectSequenceCommandGroup collectSequenceCommandGroup) {
        super(collectSequenceCommandGroup);

        this.addCommands(driveToGivenNoteCommand);
    }
}