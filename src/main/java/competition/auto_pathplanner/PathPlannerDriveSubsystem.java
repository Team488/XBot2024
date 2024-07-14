package competition.auto_pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.ReplanningConfig;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseSubsystem;
import xbot.common.math.XYPair;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PathPlannerDriveSubsystem extends BaseSubsystem {
    final DriveSubsystem drive;
    final PoseSubsystem pose;
    XYPair temp;
    double vX;
    double vY;
    double omegaRad;
    @Inject
    public PathPlannerDriveSubsystem(DriveSubsystem drive, PoseSubsystem pose) {
        this.drive = drive;
        this.pose = pose;
        this.temp = new XYPair(0,0);

        configureDriveSubsystem();
    }

    public Pose2d getPose() {
        return pose.getCurrentPose2d();
    }
    public void resetPose(Pose2d newPose) {
        pose.setCurrentPosition(newPose);
    }

    public ChassisSpeeds getChassisSpeeds() {
        return drive.getSwerveDriveKinematics().toChassisSpeeds(drive.getSwerveModuleStates());
    }

    public void driveRobotRelative(ChassisSpeeds robotRelativeSpeeds) {
        //getting chassis speeds and dividing it by top speeds, so it can be inputted into move()
        double calcX = robotRelativeSpeeds.vxMetersPerSecond / drive.getMaxTargetSpeedMetersPerSecond();
        double calcY = robotRelativeSpeeds.vyMetersPerSecond / drive.getMaxTargetSpeedMetersPerSecond();
        double calcAng = robotRelativeSpeeds.omegaRadiansPerSecond / drive.getMaxTargetTurnRate();

        //AdvantageScope Logging
        vX = robotRelativeSpeeds.vxMetersPerSecond;
        vY = robotRelativeSpeeds.vyMetersPerSecond;
        omegaRad = robotRelativeSpeeds.omegaRadiansPerSecond;

        XYPair xySpeeds = new XYPair(calcX, calcY);
        temp = xySpeeds;

        drive.move(xySpeeds, calcAng, pose.getCurrentPose2d());

    }

    public void stop() {
        drive.stop();
    }

    @Override
    public void periodic() {
        //getChassisSpeeds()
        Logger.recordOutput(getPrefix() + "SwerveModuleStates", drive.getSwerveModuleStates());
        Logger.recordOutput(getPrefix() + "Current ChassisSpeeds", getChassisSpeeds());

        //driveRobotRelative()
        Logger.recordOutput(getPrefix() + "xySpeeds", temp);
        Logger.recordOutput(getPrefix() + "vxMetersPerSecond", vX);
        Logger.recordOutput(getPrefix() + "vyMetersPerSecond", vY);
        Logger.recordOutput(getPrefix() + "omegaRadiansPerSecond:", omegaRad);
    }

    public void configureDriveSubsystem() {
        // Configure AutoBuilder last
        AutoBuilder.configureHolonomic(
                this::getPose, // Robot pose supplier
                this::resetPose, // Method to reset odometry (will be called if your auto has a starting pose)
                this::getChassisSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
                this::driveRobotRelative, // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds
                new HolonomicPathFollowerConfig( // HolonomicPathFollowerConfig, this should likely live in your Constants class
                        new PIDConstants(0, 0, 0), // Translation PID constants
                        new PIDConstants(0, 0, 0), // Rotation PID constants
                        4.5, // Max module speed, in m/s
                        0.4, // Drive base radius in meters. Distance from robot center to furthest module.
                        new ReplanningConfig() // Default path replanning config. See the API for the options here
                ),
                () -> {
                    // Boolean supplier that controls when the path will be mirrored for the red alliance
                    // This will flip the path being followed to the red side of the field.
                    // THE ORIGIN WILL REMAIN ON THE BLUE SIDE

                    var alliance = DriverStation.getAlliance();
                    if (alliance.isPresent()) {
                        return alliance.get() == DriverStation.Alliance.Red;
                    }
                    return false;
                },
                this // Reference to this subsystem to set requirements
        );
    }
}
