package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class CollectSequenceCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectSequenceCommandGroup(SetArmExtensionCommand setArmExtensionCommand,
                                       IntakeCollectorCommand intakeCollectorCommand,
                                       WaitForNoteCollectedCommand waitForNoteCollectedCommand) {
        super(waitForNoteCollectedCommand);

        setArmExtensionCommand.setTargetExtension(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        this.addCommands(setArmExtensionCommand, intakeCollectorCommand);
    }
}