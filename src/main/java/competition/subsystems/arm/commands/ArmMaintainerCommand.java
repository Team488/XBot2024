package competition.subsystems.arm.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.arm.ArmSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.CalibrationDecider;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PIDManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
public class ArmMaintainerCommand extends BaseMaintainerCommand {
    private final ArmSubsystem arm;
    private final PIDManager positionPid;
    private final OperatorInterface oi;
    private final DoubleProperty calibrationPower;

    //what is arm label and should I add it?

    @Inject
    public ArmMaintainerCommand(ArmSubsystem arm, PropertyFactory pf, HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                                PIDManager.PIDManagerFactory pidf, CalibrationDecider.CalibrationDeciderFactory calf, OperatorInterface oi){
        super(arm, pf, hvmFactory, 1, .001);
        this.arm = arm;
        this.oi = oi;
        pf.setPrefix(getName() + "/");
        calibrationPower = pf.createPersistentProperty("CalibrationPower", 0.0); // Change from 0 once we know safer

        positionPid = pidf.create(getName() + "/" + "PoisitionPID", 0.66, 0.1, 0);

    }
    @Override
    public void initialize() {
        log.info("Initializing");
        arm.setTargetValue(arm.getCurrentValue());

    }

    @Override
    protected void coastAction() {

    }

    @Override
    protected void calibratedMachineControlAction() {
        double power = positionPid.calculate(arm.getTargetValue(), arm.getCurrentValue());
        arm.setPower(power);
    }

    @Override
    protected double getErrorMagnitude() {
        return 0;
    }

    @Override
    protected Object getHumanInput() {

        return null;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0;
    }
}
