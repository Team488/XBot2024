package competition.commandgroups;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;

import javax.inject.Inject;
import javax.inject.Provider;

public class DriveToNoteAndIntake extends ParallelCommandGroup {

    @Inject
    DriveToNoteAndIntake(Provider<SwerveSimpleTrajectoryCommand> swerveSimpleTrajectoryCommandProvider){

    }
}
