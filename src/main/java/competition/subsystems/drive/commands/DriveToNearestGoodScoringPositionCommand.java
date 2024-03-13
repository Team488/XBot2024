package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class DriveToNearestGoodScoringPositionCommand extends SwerveSimpleTrajectoryCommand {
    PoseSubsystem pose;

    //LIST OUT GOOD SCORING LOCATIONS
    Pose2d subwooferTopScoringPosition = PoseSubsystem.BlueSubwooferTopScoringLocation;
    Pose2d subwooferMiddleScoringPosition = PoseSubsystem.BlueSubwooferMiddleScoringLocation;
    Pose2d subwooferBottomScoringPosition = PoseSubsystem.BlueSubwooferBottomScoringLocation;
    Pose2d spikeTop = PoseSubsystem.BlueSpikeTop;
    Pose2d spikeMiddle = PoseSubsystem.BlueSpikeMiddle;
    Pose2d spikeBottom = PoseSubsystem.BlueSpikeBottom;

    @Inject
    public DriveToNearestGoodScoringPositionCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
                                             HeadingModule.HeadingModuleFactory headingModuleFactory) {
        super(drive, pose, pf, headingModuleFactory);
        this.pose = pose;
    }

    private Pose2d getNearestGoodScoringPosition() {
        //Gets current location in the form of X and Y coordinates
        Pose2d currentPose = pose.getCurrentPose2d();

        // Gets distance between our current location and the good scoring locations
        double distanceSubwooferTopScoringPosition = PoseSubsystem.convertBlueToRedIfNeeded(subwooferTopScoringPosition)
                .getTranslation().getDistance(currentPose.getTranslation());
        double distanceSubwooferMiddleScoringPosition = PoseSubsystem.convertBlueToRedIfNeeded(subwooferMiddleScoringPosition)
                .getTranslation().getDistance(currentPose.getTranslation());
        double distanceSubwooferBottomScoringPosition = PoseSubsystem.convertBlueToRedIfNeeded(subwooferBottomScoringPosition)
                .getTranslation().getDistance(currentPose.getTranslation());

        double distanceSpikeTop = PoseSubsystem.convertBlueToRedIfNeeded(spikeTop).getTranslation().
                getDistance(currentPose.getTranslation());
        double distanceSpikeMiddle = PoseSubsystem.convertBlueToRedIfNeeded(spikeMiddle).getTranslation().
                getDistance(currentPose.getTranslation());
        double distanceSpikeBottom = PoseSubsystem.convertBlueToRedIfNeeded(spikeBottom).getTranslation().
                getDistance(currentPose.getTranslation());

        Pose2d closestGoodScoringPosition = subwooferTopScoringPosition;
        double leastDistance = distanceSubwooferTopScoringPosition;

        if (distanceSubwooferMiddleScoringPosition < leastDistance) {
            leastDistance = distanceSubwooferMiddleScoringPosition;
            closestGoodScoringPosition = subwooferMiddleScoringPosition;
        }

        if (distanceSubwooferBottomScoringPosition < leastDistance) {
            leastDistance = distanceSubwooferBottomScoringPosition;
            closestGoodScoringPosition = subwooferBottomScoringPosition;
        }

        if (distanceSpikeTop < leastDistance) {
            leastDistance = distanceSpikeTop;
            closestGoodScoringPosition = spikeTop;
        }

        if (distanceSpikeMiddle < leastDistance) {
            leastDistance = distanceSpikeMiddle;
            closestGoodScoringPosition = spikeMiddle;
        }

        if (distanceSpikeBottom < leastDistance) {
            closestGoodScoringPosition = spikeBottom;
        }

        return closestGoodScoringPosition;
    }


    @Override
    public void initialize() {
        // Gets the nearest good scoring location
        Pose2d nearestScoringPosition = getNearestGoodScoringPosition();

        ArrayList<XbotSwervePoint> points = new ArrayList<>();
        points.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(nearestScoringPosition, 10));
        logic.setKeyPoints(points);
        logic.setEnableConstantVelocity(true);
        logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        super.initialize();
    }
}
