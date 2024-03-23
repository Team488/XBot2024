package competition.commandgroups;

import competition.subsystems.drive.commands.DriveToGivenNoteCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class DriveToGivenNoteAndCollectCommandGroup extends ParallelDeadlineGroup {

    DriveToGivenNoteWithVisionCommand driveCommand;

    @Inject
    public DriveToGivenNoteAndCollectCommandGroup(DriveToGivenNoteWithVisionCommand driveToGivenNoteCommand,
                                                  CollectSequenceCommandGroup collectSequenceCommandGroup) {
        super(collectSequenceCommandGroup);
        driveCommand = driveToGivenNoteCommand;
        this.addCommands(driveToGivenNoteCommand);
    }

    public void setMaximumSpeedOverride(double maximumSpeedOverride) {
        driveCommand.setMaximumSpeedOverride(maximumSpeedOverride);
    }
}