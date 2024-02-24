package competition.subsystems.arm.commands;

import competition.electrical_contract.ElectricalContract;
import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HangingCommand extends BaseCommand {
    ArmSubsystem armSubsystem;

    @Inject
    public HangingCommand(ArmSubsystem armSubsystem) {
        addRequirements(armSubsystem);
        this.armSubsystem = armSubsystem;
    }

    @Override
    public void initialize() {
        log.info("HangingCommand initializing");
    }

    @Override
    public void execute() {
        armSubsystem.hang();
    }

}
