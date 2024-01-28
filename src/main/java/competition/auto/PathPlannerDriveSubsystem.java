package competition.auto;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.ReplanningConfig;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseSubsystem;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class PathPlannerDriveSubsystem extends BaseSubsystem {
    final DriveSubsystem drive;
    final PoseSubsystem pose;
    @Inject
    public PathPlannerDriveSubsystem(DriveSubsystem drive, PoseSubsystem pose) {
        this.drive = drive;
        this.pose = pose;

        // Configure AutoBuilder last
        AutoBuilder.configureHolonomic(
                this::getPose, // Robot pose supplier
                this::resetPose, // Method to reset odometry (will be called if your auto has a starting pose)
                this::getChassisSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
                this::driveRobotRelative, // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds
                new HolonomicPathFollowerConfig( // HolonomicPathFollowerConfig, this should likely live in your Constants class
                        new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants
                        new PIDConstants(5.0, 0.0, 0.0), // Rotation PID constants
                        0.3, // Max module speed, in m/s
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

    public Pose2d getPose() {
        return pose.getCurrentPose2d();
    }
    public void resetPose(Pose2d newPose) {
        pose.setCurrentPosition(newPose);
    }

    public ChassisSpeeds getChassisSpeeds() {
        return drive.getSwerveDriveKinematics().toChassisSpeeds(drive.getSwerveModuleStates());
    }

    public void driveRobotRelative(ChassisSpeeds speeds) {
        XYPair xySpeeds = new XYPair(getChassisSpeeds().vxMetersPerSecond, getChassisSpeeds().vyMetersPerSecond);
        drive.move(xySpeeds, getChassisSpeeds().omegaRadiansPerSecond);
    }
    public void stop() {
        drive.stop();
    }
}
