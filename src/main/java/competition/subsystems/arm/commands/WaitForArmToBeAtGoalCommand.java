package competition.subsystems.arm.commands;


import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseWaitForMaintainerCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class WaitForArmToBeAtGoalCommand extends BaseWaitForMaintainerCommand {
    final ArmSubsystem arm;
    @Inject
    public WaitForArmToBeAtGoalCommand(ArmSubsystem arm,PropertyFactory pf){
        super(arm,pf,1);
        this.arm = arm;
    }
}
