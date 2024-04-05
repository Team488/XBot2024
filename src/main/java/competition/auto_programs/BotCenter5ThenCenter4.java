package competition.auto_programs;

import competition.commandgroups.CollectSequenceCommandGroup;
import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.commandgroups.GoToGenericMidline;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToGivenNoteWithBearingVisionCommand;
import competition.subsystems.drive.commands.DriveToListOfPointsCommand;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class BotCenter5ThenCenter4 extends GoToGenericMidline {

    @Inject
    public BotCenter5ThenCenter4(AutonomousCommandSelector autoSelector, PoseSubsystem pose,
                                                 DriveSubsystem drive,
                                                 Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroupProvider,
                                                 Provider<DriveToGivenNoteWithBearingVisionCommand> driveToNoteProvider,
                                                 Provider<CollectSequenceCommandGroup> collectSequenceCommandGroupProvider,
                                                 Provider<DriveToListOfPointsCommand> driveToListOfPointsCommandProvider,
                                                 Provider<IntakeCollectorCommand> intakeCollectorCommandProvider
    ) {
        super(
                drive,
                pose,
                autoSelector,
                fireFromSubwooferCommandGroupProvider,
                driveToNoteProvider,
                collectSequenceCommandGroupProvider,
                driveToListOfPointsCommandProvider,
                intakeCollectorCommandProvider);

        setupStart();
        collectCenterNoteReturnAndFire(PoseSubsystem.CenterLine5);
        collectCenterNoteReturnAndFire(PoseSubsystem.CenterLine4);
    }
}
