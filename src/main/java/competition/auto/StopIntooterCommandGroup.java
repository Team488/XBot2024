package competition.auto;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.WarmUpShooterCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class StopIntooterCommandGroup extends ParallelCommandGroup {

    @Inject
    public StopIntooterCommandGroup(SetArmAngleCommand armToIntakingPosition,
                                    WarmUpShooterCommand stopShooter) {

        stopShooter.setTargetRpm(ShooterWheelSubsystem.TargetRPM.STOP);
        armToIntakingPosition.setArmPosition(ArmSubsystem.UsefulArmPosition.COLLECTING_FROM_GROUND);

        this.addCommands(stopShooter, armToIntakingPosition);
    }
}