package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ReconcileArmAlignmentCommand extends BaseCommand {

    ArmSubsystem armSubsystem;
    public double power = 0.05;

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
        // You only need to move one arm to reconcile, in this case, it is left.
        armSubsystem.setPower(0, power);
    }
}
