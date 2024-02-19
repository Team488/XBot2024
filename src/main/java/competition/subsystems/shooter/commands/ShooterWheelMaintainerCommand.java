package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class ShooterWheelMaintainerCommand extends BaseMaintainerCommand<ShooterWheelTargetSpeeds> {

    final ShooterWheelSubsystem wheel;
    final OperatorInterface oi;

    @Inject
    public ShooterWheelMaintainerCommand(OperatorInterface oi, ShooterWheelSubsystem wheel, PropertyFactory pf,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory) {
        super(wheel, pf, hvmFactory, 150, 0.5);
        this.oi = oi;
        this.wheel = wheel;
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
        wheel.configurePID();
    }

    @Override
    protected void coastAction() {
        // DOES NOT NEED TO HAVE ANYTHING
    }

    protected void initializeMachineControlAction() {
        wheel.resetPID();
        super.initializeMachineControlAction();
    }

    @Override
    protected void calibratedMachineControlAction() {
        var speeds = wheel.getTargetValue();


        if (speeds.representsZeroSpeed()) {
            wheel.setPower(new ShooterWheelTargetSpeeds(0.0));
            return;
        }

        if (wheel.isCalibrated()) {
            wheel.setPidSetpoints(speeds);
        }
    }

    @Override
    protected ShooterWheelTargetSpeeds getHumanInput() {
        // I THINK WE CURRENTLY DON'T HAVE ANY HUMAN INPUT.
        return new ShooterWheelTargetSpeeds(0,0);
    }

    protected void end() {
        wheel.resetWheel();
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0.0;
    }

    @Override
    protected double getErrorMagnitude() {
        var currents = wheel.getCurrentValue();
        var targets = wheel.getTargetValue();

        // Take the average of the two errors for now.
        return ((targets.upperWheelsTargetRPM - currents.upperWheelsTargetRPM)
                + (targets.lowerWheelsTargetRPM - currents.lowerWheelsTargetRPM)) / 2;
    }
}
