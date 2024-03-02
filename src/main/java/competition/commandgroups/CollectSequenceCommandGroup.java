package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.arm.commands.SetArmExtensionCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class CollectSequenceCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectSequenceCommandGroup(SetArmAngleCommand armToIntakingPosition,
                                       WarmUpShooterCommand stopShooter,
                                       SetArmExtensionCommand setArmExtensionCommand,
                                       ArmSubsystem armSubsystem,
                                       IntakeCollectorCommand intakeCollectorCommand,
                                       WaitForNoteCollectedCommand waitForNoteCollectedCommand) {
        super(waitForNoteCollectedCommand);

        setArmExtensionCommand.setTargetExtension(armSubsystem.getUsefulArmPositionExtensionInMm(
                ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND));

        this.addCommands(armToIntakingPosition, intakeCollectorCommand);
    }
}