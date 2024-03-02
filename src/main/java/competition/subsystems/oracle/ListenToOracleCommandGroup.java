package competition.subsystems.oracle;

import competition.subsystems.oracle.SuperstructureAccordingToOracleCommand;
import competition.subsystems.oracle.SwerveAccordingToOracleCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class ListenToOracleCommandGroup extends ParallelCommandGroup {

    @Inject
    public ListenToOracleCommandGroup(SwerveAccordingToOracleCommand swerveAccordingToOracleCommand,
                                      SuperstructureAccordingToOracleCommand superstructureAccordingToOracleCommand) {
        swerveAccordingToOracleCommand.logic.setEnableConstantVelocity(true);
        swerveAccordingToOracleCommand.logic.setConstantVelocity(2.8);
        this.addCommands(swerveAccordingToOracleCommand, superstructureAccordingToOracleCommand);
    }
}
