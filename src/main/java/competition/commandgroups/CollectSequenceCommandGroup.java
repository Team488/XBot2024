package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class CollectSequenceCommandGroup extends ParallelDeadlineGroup {

    @Inject
    public CollectSequenceCommandGroup(IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand,
                                       Provider<SetArmAngleCommand> setArmAngleProvider,
                                       Provider<WarmUpShooterCommand> warmUpShooterCommandProvider) {
        super(intakeUntilNoteCollectedCommand);

        var stopShooter = warmUpShooterCommandProvider.get();
        stopShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.STOP);
        var armToIntakingPosition = setArmAngleProvider.get();
        armToIntakingPosition.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        this.addCommands(stopShooter, armToIntakingPosition, intakeUntilNoteCollectedCommand);
    }
}