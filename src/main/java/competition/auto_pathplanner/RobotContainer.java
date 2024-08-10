// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package competition.auto_pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.util.PathPlannerLogging;
import competition.subsystems.collector.commands.EjectCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RobotContainer {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final SendableChooser<Command> autoChooser;
    private final Field2d field;


    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    @Inject
    public RobotContainer(DriveSubsystem drive, PoseSubsystem pose,
                          IntakeCollectorCommand intakeCollectorCommand,
                          EjectCollectorCommand ejectCollectorCommand) {

        field = new Field2d();
        SmartDashboard.putData("Field", field);

        this.drive = drive;
        this.pose = pose;

        //INTAKING NOTES
        NamedCommands.registerCommand("IntakeFirstNote", intakeCollectorCommand);
        NamedCommands.registerCommand("EjectNote", ejectCollectorCommand);

        autoChooser = AutoBuilder.buildAutoChooser(); // Default auto will be `Commands.none()`
        SmartDashboard.putData("Auto Mode", autoChooser);

        // Logging callback for current robot pose
        PathPlannerLogging.setLogCurrentPoseCallback((pose2d) -> {
            // Do whatever you want with the pose here
            field.setRobotPose(pose2d);
        });

        // Logging callback for target robot pose
        PathPlannerLogging.setLogTargetPoseCallback((pose2d) -> {
            // Do whatever you want with the pose here
            field.getObject("target pose").setPose(pose2d);
        });

        // Logging callback for the active path, this is sent as a list of poses
        PathPlannerLogging.setLogActivePathCallback((poses) -> {
            // Do whatever you want with the poses here
            field.getObject("path").setPoses(poses);
        });
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }
    public Command getFast4NoteFarCommand() {
        return new PathPlannerAuto("4noteAutoFarBot");
    }
    public Pose2d getFast4NoteFarPose() {
        return PathPlannerAuto.getStaringPoseFromAutoFile("4noteAutoFarBot");
    }
    public Command getPodiumMidCommand() {
        return new PathPlannerAuto("PodiumMid");
    }
    public Command getIntakeNoteTestCommand() {
        return new PathPlannerAuto("IntakeNoteTest");
    }
    public Command getBNBCommand() {
        return new PathPlannerAuto("BNB");
    }
    public Command getTranslationX() {
//        PathPlannerPath path = PathPlannerPath.fromPathFile("TranslationX");
//        return AutoBuilder.followPath(path);
        return new PathPlannerAuto("TranslatingX");
    }

    public Command getTranslationXandY() {
        return new PathPlannerAuto("TranslatingXandY");

    }

    public Command getPoseTestCommand() {
        return new PathPlannerAuto("PoseTest");

    }

    public Command getTranslationXandYRotate() {
        return new PathPlannerAuto("XYandRotating");
    }
    public Pose2d getTranslationXYPose() {
        return PathPlannerAuto.getStaringPoseFromAutoFile("TranslatingXandY");
    }

}