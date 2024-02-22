package competition.commandgroups;

import competition.subsystems.collector.commands.WaitForNoteCollectedCommand;
import competition.subsystems.oracle.DynamicOracle;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

public class DriveToNoteAndCollectCommandGroup extends ParallelDeadlineGroup{
    Pose2d notePosition;
    final Provider<SwerveSimpleTrajectoryCommand> swerveProvider;
    final DynamicOracle oracle;
    final CollectCommandGroup collect;

    @Inject
    DriveToNoteAndCollectCommandGroup(Provider<SwerveSimpleTrajectoryCommand> swerveProvider,
                         WaitForNoteCollectedCommand waitForNote, DynamicOracle oracle, CollectCommandGroup collect) {
        //sets the deadline to waitForNoteCollected
        super(waitForNote);

        this.swerveProvider = swerveProvider;
        this.oracle = oracle;
        this.collect = collect;
    }

    public void setNotePosition(Pose2d notePosition) {
        this.notePosition = notePosition;

        var swerveToNote = swerveProvider.get();
        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                notePosition,10));
        // when driving to a note, the robot must face backwards, as the robot's intake is on the back
        swerveToNote.logic.setKeyPoints(swervePoints);
        swerveToNote.logic.setAimAtGoalDuringFinalLeg(true);
        swerveToNote.logic.setDriveBackwards(true);
        swerveToNote.logic.setEnableConstantVelocity(true);
        swerveToNote.logic.setConstantVelocity(1);
        swerveToNote.logic.setFieldWithObstacles(oracle.getFieldWithObstacles());



        this.addCommands(swerveToNote.alongWith(collect));
    }
}
