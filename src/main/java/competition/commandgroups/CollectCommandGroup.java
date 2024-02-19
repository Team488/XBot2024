package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class CollectCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectCommandGroup(WaitForNoteCollectedCommand waitForNote,
                               SetArmAngleCommand setArmAngle,
                               IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand) {
        super(waitForNote);

        // move arm to collecting position
        setArmAngle.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);
        this.addCommands(setArmAngle.alongWith(intakeUntilNoteCollectedCommand));
    }
}
