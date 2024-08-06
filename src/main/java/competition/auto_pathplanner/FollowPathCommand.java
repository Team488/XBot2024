package competition.auto_pathplanner;

import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.PathPlannerTrajectory;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;

import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class FollowPathCommand extends BaseCommand {
    /**
     * Timer object
     */
    private final Timer timer = new Timer();

    /**
     * Drivetrain object to access subsystem.
     */
    private final DriveSubsystem drive;

    PathPlannerPath path;

    /**
     * {@link PathPlannerTrajectory} to follow
     */
    private PathPlannerTrajectory trajectory;

    PIDController rotationPID;

    PIDController translationPID;
    PoseSubsystem pose;

    @Inject
    public FollowPathCommand(DriveSubsystem drive, PoseSubsystem pose) {
        this.drive = drive;
        this.pose = pose;

        rotationPID = new PIDController(0.01, 0, 0.02);
//        rotationPID = new PIDController(0, 0, 0 );
        translationPID = new PIDController(0, 0, 0);
//        translationPID = new PIDController(0, 0, 0);

        addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        rotationPID.reset();
        translationPID.reset();

        trajectory = new PathPlannerTrajectory(
                path,
                drive.getSwerveDriveKinematics().toChassisSpeeds(drive.getSwerveModuleStates()),
                pose.getCurrentPose2d().getRotation());

        this.timer.restart();
    }

    @Override
    public void execute() {
        double currentTime = this.timer.get();
        // Determine desired state based on where the robot should be at the current time in the path
        PathPlannerTrajectory.State desiredState = trajectory.sample(currentTime);
        var currentPose = pose.getCurrentPose2d();

        Rotation2d heading = desiredState.heading;

        // Calculate our target velocity based on current pose and desired state
        var vx = desiredState.velocityMps * Math.cos(heading.getRadians());
        var vy = desiredState.velocityMps * Math.sin(heading.getRadians());
        var desiredThetaSpeeds = rotationPID.calculate(
                currentPose.getRotation().getRadians(), desiredState.targetHolonomicRotation.getRadians());

        double xFeedback = translationPID.calculate(
                currentPose.getX(), desiredState.getTargetHolonomicPose().getX());
        double yFeedback = translationPID.calculate(
                currentPose.getY(), desiredState.getTargetHolonomicPose().getY());

        ChassisSpeeds chassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                vx + xFeedback, vy + yFeedback, desiredThetaSpeeds, currentPose.getRotation());


        driveRobotRelative(chassisSpeeds);


        Logger.recordOutput("PathFollowing/DesiredStatePose", desiredState.getTargetHolonomicPose());
        Logger.recordOutput("PathFollowing/DesiredChassisSpeeds", chassisSpeeds);
    }

    @Override
    public void end(boolean interrupted) {
        log.info("Follow Path Command has ended");
        this.timer.stop(); // Stop timer
        drive.stop();
    }

    @Override
    public boolean isFinished() {
        return timer.hasElapsed(trajectory.getTotalTimeSeconds() + 2);
    }

    public Pose2d getStart() {
        return trajectory.getInitialState().getTargetHolonomicPose();
    }

    public void driveRobotRelative(ChassisSpeeds robotRelativeSpeeds) {
        //AdvantageScope Logging
        double vX = robotRelativeSpeeds.vxMetersPerSecond;
        double vY = robotRelativeSpeeds.vyMetersPerSecond;
        double omegaRad = robotRelativeSpeeds.omegaRadiansPerSecond;

        //getting chassis speeds and dividing it by top speeds, so it can be inputted into move()
        double calcX = robotRelativeSpeeds.vxMetersPerSecond / drive.getMaxTargetSpeedMetersPerSecond();
        double calcY = robotRelativeSpeeds.vyMetersPerSecond / drive.getMaxTargetSpeedMetersPerSecond();
        double calcAng = robotRelativeSpeeds.omegaRadiansPerSecond / drive.getMaxTargetTurnRate();

        XYPair xySpeeds = new XYPair(calcX, calcY);

        drive.move(xySpeeds, calcAng, pose.getCurrentPose2d());

        Logger.recordOutput("PathPlanner/FollowPathCommand/vxMetersPerSecond", vX);
        Logger.recordOutput("PathPlanner/FollowPathCommand/vyMetersPerSecond", vY);
        Logger.recordOutput("PathPlanner/FollowPathCommand/omegaRadPerSecond", omegaRad);
    }

    public void setPath(PathPlannerPath path) {
        this.path = path;
    }
}