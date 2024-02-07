package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.SetArmAngleCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.SetShooterSpeedFromLocationCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class FireNoteCommandGroup extends ParallelCommandGroup {

    public DoubleProperty waitTimeAlongShooting;

    @Inject
    FireNoteCommandGroup(PoseSubsystem pose,
                         ArmSubsystem arm,
                         SetArmAngleCommand setArmAngleCommand,
                         SetShooterSpeedFromLocationCommand setShooterSpeedFromLocationCommand,
                         PropertyFactory pf,
                         FireWhenReadyCommand fireWhenReadyCommand) {

        waitTimeAlongShooting = pf.createPersistentProperty("FireNoteWaitTime", 0.5);

        // Set arm angle
        setArmAngleCommand.setTargetAngle(arm.getArmAngleFromDistance(pose.getDistanceFromSpeaker()));
        this.addCommands(setArmAngleCommand);

        // Set shooter rpm
        this.addCommands(setShooterSpeedFromLocationCommand);

        // Call Alan's fire command along with timeout
        this.addCommands(fireWhenReadyCommand.withTimeout(waitTimeAlongShooting.get()));
    }
}
