// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package competition.auto_pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import competition.subsystems.collector.commands.IntakeCollectorCommand;
import competition.subsystems.collector.commands.IntakeCollectorCommand_Factory;
import competition.subsystems.collector.commands.IntakeUntilNoteCollectedCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class RobotContainer {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;

    private final SendableChooser<Command> autoChooser;

    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    @Inject
    public RobotContainer(DriveSubsystem drive, PoseSubsystem pose,
                          Provider<IntakeCollectorCommand> intakeCollectorCommandProvider,
                          ) {

        this.drive = drive;
        this.pose = pose;


        //INTAKING NOTES
        var intakeFirstNote = intakeCollectorCommandProvider.get();
        NamedCommands.registerCommand("IntakeFirstNote", intakeFirstNote);
        var intakeSecondNote = intakeCollectorCommandProvider.get();
        NamedCommands.registerCommand("IntakeSecondNote", intakeSecondNote);
        var intakeThirdNote = intakeCollectorCommandProvider.get();
        NamedCommands.registerCommand("IntakeThirdNote", intakeThirdNote);
        var intakeFourthNote = intakeCollectorCommandProvider.get();
        NamedCommands.registerCommand("IntakeFourthNote", intakeFourthNote);
        var intakeFifthNote = intakeCollectorCommandProvider.get();
        NamedCommands.registerCommand("IntakeFifthNote", intakeFifthNote);

        //FIRING EVERYWHERE
        var prepareArm1 = prepareEverywhereCommandGroupProvider.get();
        NamedCommands.registerCommand("PrepareEverywhere1", prepareArm1);
        var prepareArm2 = prepareEverywhereCommandGroupProvider.get();
        NamedCommands.registerCommand("PrepareEverywhere2", prepareArm2);
        var prepareArm3 = prepareEverywhereCommandGroupProvider.get();
        NamedCommands.registerCommand("PrepareEverywhere3", prepareArm3);


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
    public Command getFast4NoteFarCommand() {
        return new PathPlannerAuto("5noteBot");
    }
    public Command getBotMidFarCommand() {
        return new PathPlannerAuto("BotMidFarShot");
    }

}