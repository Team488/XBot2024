package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ReconcileArmAlignmentCommand extends BaseCommand {

    ArmSubsystem armSubsystem;
    double power;

    @Inject
    public ReconcileArmAlignmentCommand(ArmSubsystem armSubsystem) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    public void setReconcilePower(double power) {
        this.power = power;
    }

    @Override
    public void initialize() {
        System.out.println("ReconcileArmAlignmentCommand initializing");
    }

    @Override
    public void execute() {
        armSubsystem.setPower(power, 0);
    }
}
