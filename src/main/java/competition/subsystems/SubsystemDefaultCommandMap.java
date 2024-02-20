package competition.subsystems;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.ArmMaintainerCommand;
import competition.subsystems.arm.commands.DefaultToCurrentPositionCommand;
import competition.subsystems.arm.commands.StopArmCommand;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.collector.commands.StopCollectorCommand;
import competition.subsystems.schoocher.ScoocherSubsystem;
import competition.subsystems.schoocher.commands.StopScoocherCommand;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import competition.subsystems.shooter.commands.ShooterWheelMaintainerCommand;
import competition.subsystems.shooter.commands.WarmUpShooterRPMCommand;
import xbot.common.injection.swerve.FrontLeftDrive;
import xbot.common.injection.swerve.FrontRightDrive;
import xbot.common.injection.swerve.RearLeftDrive;
import xbot.common.injection.swerve.RearRightDrive;
import xbot.common.injection.swerve.SwerveComponent;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;

/**
 * For setting the default commands on subsystems
 */
@Singleton
public class SubsystemDefaultCommandMap {

    @Inject
    public SubsystemDefaultCommandMap() {}

    @Inject
    public void setupDriveSubsystem(DriveSubsystem driveSubsystem, SwerveDriveWithJoysticksCommand command) {
        driveSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void setupArmSubsystem(ArmSubsystem armSubsystem,
                                  ArmMaintainerCommand command,
                                  DefaultToCurrentPositionCommand defaultToCurrentPositionCommand) {
        armSubsystem.setDefaultCommand(command);
        armSubsystem.getSetpointLock().setDefaultCommand(defaultToCurrentPositionCommand);
    }
    @Inject
    public void setupScoocherSubsystem(ScoocherSubsystem scoocherSubsystem, StopScoocherCommand command){
        scoocherSubsystem.setDefaultCommand(command);
    }
    @Inject
    public void setUpCollectorSubsystem(CollectorSubsystem collectorSubsystem, StopCollectorCommand command) {
        collectorSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void setupShooterWheelSubsystem(ShooterWheelSubsystem shooterWheelSubsystem,
                                           ShooterWheelMaintainerCommand command,
                                           WarmUpShooterRPMCommand setToZero) {
        shooterWheelSubsystem.setDefaultCommand(command);
        setToZero.setTargetRpm(new ShooterWheelTargetSpeeds(0.0, 0.0));
        shooterWheelSubsystem.getSetpointLock().setDefaultCommand(setToZero);
    }
}
