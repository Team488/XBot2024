package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.ArmSubsystem_Factory;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseWaitForMaintainerCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class WaitForArmToBeAtGoalCommand extends BaseWaitForMaintainerCommand {
    final ArmSubsystem arm;
    private DoubleProperty timeoutProperty;
    private double startTime;
    @Inject
    public WaitForArmToBeAtGoalCommand(ArmSubsystem arm, PropertyFactory pf, double defaultTimeout){
        super(arm,pf,defaultTimeout);

        this.arm = arm;
    }
    @Override
    public void initialize() {
        this.startTime = XTimer.getFPGATimestamp();
    }

    @Override
    public void execute() {
        //nothing to do
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }
}
