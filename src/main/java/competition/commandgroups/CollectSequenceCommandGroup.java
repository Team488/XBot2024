package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class CollectSequenceCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectSequenceCommandGroup(IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand,
                                       SetArmAngleCommand armToIntakingPosition,
                                       WarmUpShooterCommand stopShooter) {
        super(intakeUntilNoteCollectedCommand);

        stopShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.STOP);
        armToIntakingPosition.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        this.addCommands(stopShooter, armToIntakingPosition, intakeUntilNoteCollectedCommand);
    }
}