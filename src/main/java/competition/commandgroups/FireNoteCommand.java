package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterDistanceToRpmConverter;
import competition.subsystems.shooter.commands.SetShooterSpeedFromLocationCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.properties.DoubleProperty;

import javax.inject.Inject;

public class FireNoteCommand extends ParallelCommandGroup {

    public DoubleProperty waitTimeAfterShooting;

    @Inject
    FireNoteCommand(PoseSubsystem pose,
                    ArmSubsystem arm,
                    SetArmAngleCommand setArmAngleCommand,
                    SetShooterSpeedFromLocationCommand setShooterSpeedFromLocationCommand) {

        // from current position,
        //set arm and shooter
        //and then fire when ready
        //wait a little (DoubleProperty) amount of time before finishing so note clears shooter for sure

        double distanceFromSpeaker = pose.getDistanceFromSpeaker();
        double armAngle = arm.getArmAngleFromDistance(distanceFromSpeaker);

        // Set arm angle
        setArmAngleCommand.setTargetAngle(armAngle);
        this.addCommands(setArmAngleCommand);

        // Set shooter rpm
        this.addCommands(setShooterSpeedFromLocationCommand);


    }
}
