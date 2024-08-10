package competition.auto_pathplanner;

import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.PathPlannerTrajectory;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;

import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.math.PIDManager;
import xbot.common.math.XYPair;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

import javax.inject.Inject;

public class FollowPathCommand extends BaseCommand {
    private final Timer timer = new Timer();
    private final DriveSubsystem drive;
    private PathPlannerPath path;
    private final PoseSubsystem pose;
    private PathPlannerTrajectory trajectory;
    HeadingModule headingModule;
    PIDManager goalHeadingPidManager;
    PIDController translationXPID;
    PIDController translationYPID;

    @Inject
    public FollowPathCommand(DriveSubsystem driveSubsystem, PoseSubsystem pose,
                             HeadingModule.HeadingModuleFactory headingModuleFactory) {
        this.drive = driveSubsystem;
        this.pose = pose;

        translationXPID = drive.getPathFollowTranslationXPID();
        translationYPID = drive.getPathFollowTranslationYPID();

        goalHeadingPidManager = drive.getPathFollowGoalHeadingPid();
        this.headingModule = headingModuleFactory.create(goalHeadingPidManager);

//        headingModule = headingModuleFactory.create(drive.getPathPlannerGoalHeadingPid());
//        translationXPID = drive.getPathPlannerTranslationXPid();
//        translationYPID = drive.getPathPlannerTranslationYPid();

        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        translationXPID.reset();
        translationYPID.reset();
        headingModule.reset();

        trajectory = new PathPlannerTrajectory(
                path,
                drive.getSwerveDriveKinematics().toChassisSpeeds(drive.getSwerveModuleStates()),
                pose.getCurrentPose2d().getRotation());

        this.timer.restart();
    }

    @Override
    public void execute() {
        double currentTime = this.timer.get();
        PathPlannerTrajectory.State desiredState =  trajectory.sample(currentTime);
        Pose2d currentPose = pose.getCurrentPose2d();

        // Converting the velocity from a vector to x and y
        double vx = desiredState.velocityMps * Math.cos(desiredState.heading.getRadians());
        double vy = desiredState.velocityMps * Math.sin(desiredState.heading.getRadians());

        // PID to keep robot on the path
        double vxFeedBack = translationXPID.calculate
                (currentPose.getX(), desiredState.getTargetHolonomicPose().getX());
        double vyFeedBack = translationYPID.calculate(
                currentPose.getY(), desiredState.getTargetHolonomicPose().getY());

        // Calculate headingPower
        double headingPower = headingModule.calculateHeadingPower(desiredState.targetHolonomicRotation.getDegrees());

        // Convert Field relative chassis speeds to robot relative
        ChassisSpeeds chassisSpeeds =
                ChassisSpeeds.fromFieldRelativeSpeeds(vx + vxFeedBack, vy + vyFeedBack,
                        headingPower, currentPose.getRotation());

        driveRobotRelative(chassisSpeeds);

        Logger.recordOutput("PathPlanner/FollowPathCommand/DesiredStatePose", desiredState.getTargetHolonomicPose());
        Logger.recordOutput("PathPlanner/FollowPathCommand/desiredVXPerSecond", vx);
        Logger.recordOutput("PathPlanner/FollowPathCommand/desiredVYPerSecond", vy);
        Logger.recordOutput("PathPlanner/FollowPathCommand/vxFeedBack", vxFeedBack);
        Logger.recordOutput("PathPlanner/FollowPathCommand/vyFeedback", vyFeedBack);
        Logger.recordOutput("PathPlanner/FollowPathCommand/headingPower", headingPower);
    }

    @Override
    public boolean isFinished() {
        return timer.hasElapsed(trajectory.getTotalTimeSeconds());
    }

    @Override
    public void end(boolean interrupted) {
        log.info("Command has ended");
        this.timer.stop(); // Stop timer
        driveRobotRelative(new ChassisSpeeds(0, 0, 0));
        drive.stop();
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