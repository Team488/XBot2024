package competition.auto_programs;

import competition.subsystems.oracle.SuperstructureAccordingToOracleCommand;
import competition.subsystems.oracle.SwerveAccordingToOracleCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import javax.inject.Inject;

public class ListenToOracleAuto extends ParallelCommandGroup {

    @Inject
    public ListenToOracleAuto(SwerveAccordingToOracleCommand swerveAccordingToOracleCommand,
                              SuperstructureAccordingToOracleCommand superstructureAccordingToOracleCommand) {
        swerveAccordingToOracleCommand.logic.setEnableConstantVelocity(true);
        swerveAccordingToOracleCommand.logic.setConstantVelocity(2.8);
        this.addCommands(swerveAccordingToOracleCommand, superstructureAccordingToOracleCommand);
    }
}
