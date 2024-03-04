package competition.commandgroups;

import competition.subsystems.arm.commands.SetArmExtensionForSpeakerFromLocation;
import competition.subsystems.shooter.commands.FireWhenReadyCommand;
import competition.subsystems.shooter.commands.SetShooterSpeedFromLocationCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;

import javax.inject.Inject;

public class FireNoteCommandGroup extends ParallelDeadlineGroup {

    @Inject
    FireNoteCommandGroup(SetShooterSpeedFromLocationCommand setShooterSpeedFromLocationCommand,
                         FireWhenReadyCommand fireWhenReadyCommand,
                         SetArmExtensionForSpeakerFromLocation setArmFromLocationCommand) {

        // Fire note when arm angle and shooter rpm within our range
        super(fireWhenReadyCommand);

        // Set arm angle
        this.addCommands(setArmFromLocationCommand);

        // Set shooter rpm
        this.addCommands(setShooterSpeedFromLocationCommand);


    }
}
