package competition.auto_programs;

import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class GriefMiddle extends SequentialCommandGroup {

    @Inject
    public GriefMiddle(
            Provider<SwerveSimpleTrajectoryCommand> swerveSimpleTrajectoryCommandProvider,
            Provider<FireFromSubwooferCommandGroup> fireFromSubwooferCommandGroup,
            EjectCollectorCommand ejectCollector,
            EjectScoocherCommand ejectScoocher,
            DriveSubsystem drive,
            PoseSubsystem pose)
    {
        // Start at the bot, maybe?

        var startSpeakerBot = pose.createSetPositionCommand(
                () -> PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.BlueSubwooferBottomScoringLocation));
        this.addCommands(startSpeakerBot);

        // Shoot 1st note
        var shoot = fireFromSubwooferCommandGroup.get();
        this.addCommands(shoot);

        // Drive to lower blue line waypoint
        var driveToLowerWing = swerveSimpleTrajectoryCommandProvider.get();
        driveToLowerWing.logic.setKeyPointsProvider(this::goToBottomWing);
        driveToLowerWing.logic.setEnableConstantVelocity(true);
        driveToLowerWing.logic.setConstantVelocity(drive.getSuggestedAutonomousMaximumSpeed());
        // Keep driving!
        driveToLowerWing.logic.setStopWhenFinished(false);

        this.addCommands(driveToLowerWing);

        // Drive to lowest note and start spinning
        var driveToCenter5 = swerveSimpleTrajectoryCommandProvider.get();
        driveToCenter5.logic.setKeyPointsProvider(this::goAcrossCenterLine);
        driveToCenter5.setConstantRotationPowerSupplier(this::getRotationPower);
        driveToCenter5.logic.setEnableConstantVelocity(true);
        driveToCenter5.logic.setConstantVelocity(drive.getSuggestedAutonomousMaximumSpeed());

        this.addCommands(driveToCenter5.alongWith(ejectCollector, ejectScoocher));
    }

    public List<XbotSwervePoint> goToBottomWing() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueBottomWing, 10));
        return points;
    }

    public List<XbotSwervePoint> goAcrossCenterLine() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine5, 10));
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.CenterLine1, 10));
        return points;
    }

    public double getRotationPower() {
        double rotationPower = 0.5;
        if (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Blue) {
            // on blue alliance, spin positive
            return rotationPower;
        } else{
            // on red alliance, spin negative
            return -rotationPower;
        }

    }


}
