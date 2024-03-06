package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.arm.commands.WaitForArmToBeAtGoalCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class CollectCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectCommandGroup(WaitForNoteCollectedCommand waitForNote,
                               IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand,
                               Provider<SetArmExtensionCommand> setArmExtensionCommandProvider,
                               WaitForArmToBeAtGoalCommand waitForArm) {
        super(waitForNote);

        // move arm to collecting position
        var extendToGround = setArmExtensionCommandProvider.get();
        extendToGround.setTargetExtension(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        this.addCommands(extendToGround.alongWith(waitForArm).andThen(intakeUntilNoteCollectedCommand));
    }
}
