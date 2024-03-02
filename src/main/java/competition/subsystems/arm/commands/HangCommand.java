package competition.subsystems.arm.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HangCommand extends BaseCommand {
    ArmSubsystem armSubsystem;
    ArmMaintainerCommand armMaintainer;
    final OperatorInterface oi;

    @Inject
    public HangCommand(ArmSubsystem armSubsystem, OperatorInterface oi, ArmMaintainerCommand armMaintainer) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
        this.armMaintainer = armMaintainer;
        this.oi = oi;
    }

    @Override
    public void initialize() {
        log.info("HangCommand initializing");
    }

    @Override
    public void execute() {
        armSubsystem.hang();
    }
}



