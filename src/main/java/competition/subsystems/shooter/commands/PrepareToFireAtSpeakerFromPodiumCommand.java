package competition.subsystems.shooter.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class PrepareToFireAtSpeakerFromPodiumCommand extends BaseCommand {
    ArmSubsystem armSubsystem;
    ShooterWheelSubsystem shooterWheelSubsystem;


    @Inject
    public PrepareToFireAtSpeakerFromPodiumCommand(ArmSubsystem armSubsystem, ShooterWheelSubsystem shooterWheelSubsystem) {
        this.armSubsystem = armSubsystem;
        this.shooterWheelSubsystem = shooterWheelSubsystem;
        addRequirements(armSubsystem, shooterWheelSubsystem);
    }

    @Override
    public void initialize() {
        shooterWheelSubsystem.setTargetRPM(ShooterWheelSubsystem.TargetRPM.PODIUM_SHOT);
        armSubsystem.setTargetAngle(armSubsystem.getUsefulArmPositionAngle(ArmSubsystem.UsefulArmPosition.FIRING_FROM_PODIUM));
        log.info("PrepareToFireAtSpeakerFromPodiumCommand initializing..");
    }
    @Override
    public void execute() {
        // Nothing should be added here right or?
    }
}

