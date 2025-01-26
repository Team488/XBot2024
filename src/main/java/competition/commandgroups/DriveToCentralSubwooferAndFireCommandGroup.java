package competition.commandgroups;

import competition.subsystems.drive.commands.DriveToCentralSubwooferCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class DriveToCentralSubwooferAndFireCommandGroup extends SequentialCommandGroup {

    double interstageTimeout = 7;

    @Inject
    DriveToCentralSubwooferAndFireCommandGroup(FireFromSubwooferCommandGroup fireFromSubwooferCommandGroup,
                                               DriveToCentralSubwooferCommand driveToCentralSubwooferCommand) {
        this.addCommands(driveToCentralSubwooferCommand.withTimeout(interstageTimeout));
        this.addCommands(fireFromSubwooferCommandGroup);
    }
}
