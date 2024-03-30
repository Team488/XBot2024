package competition.auto_programs;

import competition.commandgroups.FireFromSubwooferCommandGroup;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.schoocher.commands.EjectScoocherCommand;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.pose.BasePoseSubsystem;
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
        driveToLowerWing.logic.setConstantVelocity(drive.getSuggestedAutonomousExtremeSpeed());
        // Keep driving!
        driveToLowerWing.logic.setStopWhenFinished(false);
        driveToLowerWing.logic.setDriveBackwards(true);
        driveToLowerWing.logic.setAimAtGoalDuringFinalLeg(true);

        this.addCommands(driveToLowerWing);

        // Drive to lowest note pointing north
        var justDriveToCenter5 = swerveSimpleTrajectoryCommandProvider.get();
        justDriveToCenter5.logic.setKeyPointsProvider(this::goToCenter5);
        justDriveToCenter5.logic.setEnableConstantVelocity(true);
        justDriveToCenter5.logic.setStopWhenFinished(false);
        justDriveToCenter5.logic.setConstantVelocity(drive.getSuggestedAutonomousExtremeSpeed());
        justDriveToCenter5.logic.setEnableSpecialAimTarget(true);
        justDriveToCenter5.logic.setSpecialAimTarget(
                new Pose2d(
                        new Translation2d(
                            BasePoseSubsystem.fieldXMidpointInMeters,
                                3000000),
                        Rotation2d.fromDegrees(0)));

        this.addCommands(justDriveToCenter5);

        var driveToCenter1 = swerveSimpleTrajectoryCommandProvider.get();
        driveToCenter1.logic.setKeyPointsProvider(this::goUpLineToCenter1);
        driveToCenter1.setConstantRotationPowerSupplier(this::getRotationPower);
        driveToCenter1.logic.setEnableConstantVelocity(true);
        driveToCenter1.logic.setConstantVelocity(drive.getSuggestedAutonomousMaximumSpeed());

        this.addCommands(driveToCenter1.alongWith(ejectCollector, ejectScoocher));
    }

    public List<XbotSwervePoint> goToBottomWing() {
        var points = new ArrayList<XbotSwervePoint>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(PoseSubsystem.BlueBottomWing, 10));
        return points;
    }

    double globalXAdjustment = 0.25;

    private Pose2d shiftCenterlineX(Pose2d poseToAdjust) {
        return new Pose2d(new Translation2d(
                poseToAdjust.getX() + globalXAdjustment,
                poseToAdjust.getY()),
                poseToAdjust.getRotation());
    }

    public List<XbotSwervePoint> goToCenter5() {
        var points = new ArrayList<XbotSwervePoint>();
        var adjusted5 = shiftCenterlineX(PoseSubsystem.CenterLine5);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(adjusted5, 10));
        return points;
    }

    public List<XbotSwervePoint> goUpLineToCenter1() {
        var points = new ArrayList<XbotSwervePoint>();
        var adjusted1 = shiftCenterlineX(PoseSubsystem.CenterLine1);
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(adjusted1, 10));
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
