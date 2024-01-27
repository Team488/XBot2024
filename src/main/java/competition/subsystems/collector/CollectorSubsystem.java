package competition.subsystems.collector;

import competition.electrical_contract.ElectricalContract;
import org.littletonrobotics.junction.Logger;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMaxPIDProperties;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSubsystem extends BaseSubsystem implements DataFrameRefreshable {
    public final XCANSparkMax collectorMotor;
    public final DoubleProperty intakePower;
    public final DoubleProperty ejectPower;
    private IntakeState intakeState;
    private final XDigitalInput noteSensor;
    private final ElectricalContract contract;


    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED
    }

    @Inject
    public CollectorSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                              ElectricalContract electricalContract, XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        this.contract = electricalContract;
        this.collectorMotor = sparkMaxFactory.createWithoutProperties(contract.getCollectorMotor(), getPrefix(), "CollectorMotor");
        this.noteSensor = xDigitalInputFactory.create(contract.getNoteSensorDio());

        pf.setPrefix(this);
        intakePower = pf.createPersistentProperty("intakePower",0.1);
        ejectPower = pf.createPersistentProperty("ejectPower",0.1);

        this.intakeState = IntakeState.STOPPED;
    }

    public IntakeState getIntakeState(){
        return intakeState;
    }
    public void intake(){
        collectorMotor.set(intakePower.get());
        intakeState = IntakeState.INTAKING;
    }
    public void eject(){
        collectorMotor.set(ejectPower.get());
        intakeState = IntakeState.EJECTING;
    }
    public void stop(){
        collectorMotor.set(0);
        intakeState = IntakeState.STOPPED;
    }

    public boolean getGamePieceCollected() {
        return noteSensor.get();
    }

    @Override
    public void periodic() {
        Logger.recordOutput(getPrefix() + "HasGamePiece", getGamePieceCollected());
    }

    @Override
    public void refreshDataFrame() {
        collectorMotor.refreshDataFrame();
        noteSensor.refreshDataFrame();
    }
}