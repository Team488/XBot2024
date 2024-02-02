package competition.subsystems.arm.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.CalibrationDecider;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PIDManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
public class ArmMaintainerCommand extends BaseMaintainerCommand {
    private final ArmSubsystem arm;
    private final PIDManager positionPid;
    // oi used for human input
    private final OperatorInterface oi;


    @Inject
    public ArmMaintainerCommand(ArmSubsystem arm, PropertyFactory pf,
                                HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                                PIDManager.PIDManagerFactory pidf,
                                CalibrationDecider.CalibrationDeciderFactory calf, OperatorInterface oi){
        super(arm, pf, hvmFactory, 1, .001);
        this.arm = arm;
        this.oi = oi;
        pf.setPrefix(getName() + "/");
        pf.setDefaultLevel(Property.PropertyLevel.Debug);
        positionPid = pidf.create(getName() + "/" + "PoisitionPID", 0.66, 0.1, 0);


    }
    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setTargetValue(arm.getCurrentValue());
    }

    @Override
    protected void coastAction() {
        //Don't think anything is needed here...
    }

    @Override
    protected void calibratedMachineControlAction() {
        double power = positionPid.calculate(arm.getTargetValue(), arm.getCurrentValue());
        arm.setPower(power);
    }

    @Override
    protected double getErrorMagnitude() {
            double current = arm.getCurrentValue();
            double target = arm.getTargetValue();
            double armError = 0;
            if(current != target){
                armError = Math.abs(target - current);
                return armError;
            }
        return armError;
    }

    @Override
    protected Double getHumanInput() {
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0.0;
    }
}
