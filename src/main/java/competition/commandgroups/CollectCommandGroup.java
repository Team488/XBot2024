package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class CollectCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectCommandGroup(WaitForNoteCollectedCommand waitForNote,
                               IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand,
                               Provider<SetArmAngleCommand> setArmAngleProvider) {
        super(waitForNote);

        // move arm to collecting position
        var extendToGround = setArmAngleProvider.get();
        var reset = setArmAngleProvider.get();
        extendToGround.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);
        reset.setArmPosition(ArmSubsystem.UsefulArmPosition.STARTING_POSITION);
        this.addCommands(extendToGround.alongWith(intakeUntilNoteCollectedCommand).andThen(reset));
    }
}
