// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package competition.auto;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.path.PathPlannerPath;

import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
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

    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    @Inject
    public RobotContainer(DriveSubsystem drive, PoseSubsystem pose,
                          IntakeCollectorCommand intakeCollectorCommand,
                          StopCollectorCommand stopCollectorCommand,
                          IntakeUntilNoteCollectedCommand intakeUntilNoteCollectedCommand) {

        this.drive = drive;
        this.pose = pose;

        //testing on 2023
        NamedCommands.registerCommand("IntakeCollectorCommand", intakeCollectorCommand);
        NamedCommands.registerCommand("StopCollectorCommand", stopCollectorCommand);


        //TODO: 2024
        //fire while moving to note

        //intake note
        NamedCommands.registerCommand("IntakeUntilNoteCollectedCommand", intakeUntilNoteCollectedCommand);

        //shooting from spike (subwoofer RPM prob)

        autoChooser = AutoBuilder.buildAutoChooser(); // Default auto will be `Commands.none()`
        SmartDashboard.putData("Auto Mode", autoChooser);
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }

    public Command getTestPathCommand() {
        PathPlannerPath path = PathPlannerPath.fromPathFile("TESTPATH");
        path.preventFlipping = true;
        return AutoBuilder.followPath(path);
    }
    public Command getFourNoteAutoCommand() {
        return new PathPlannerAuto("4noteAutoClose");
    }
    public Command getMidNoteCommand() {
        return new PathPlannerAuto("midnote");
    }
    public Command getFast4NoteFarCommand() {
        return new PathPlannerAuto("4noteAutoFar");
    }
    public Command getFast4NoteCloseCommand() {
        return new PathPlannerAuto("Fast4NoteAutoClose");
    }
    public Command getMarkerTestAutoCommand() {
        return new PathPlannerAuto("MarkerTestAuto");
    }


}