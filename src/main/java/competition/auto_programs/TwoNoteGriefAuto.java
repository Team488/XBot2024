package competition.auto_programs;

import competition.commandgroups.FireNoteCommandGroup;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;

import javax.inject.Provider;

public class TwoNoteGriefAuto extends SequentialCommandGroup {
    public TwoNoteGriefAuto(FireNoteCommandGroup fireNote,
                            Provider<SwerveSimpleTrajectoryCommand> swerveToEdge,
                            PoseSubsystem pose){

    }
}
