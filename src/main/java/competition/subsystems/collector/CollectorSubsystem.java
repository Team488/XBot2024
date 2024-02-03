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
    private final XDigitalInput inControlNoteSensor;
    private final XDigitalInput readyToFireNoteSensor;
    private final ElectricalContract contract;
    private final DoubleProperty firePower;
    private final DoubleProperty intakePowerInControlMultiplier;


    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED,
        FIRING
    }

    @Inject
    public CollectorSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                              ElectricalContract electricalContract, XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        this.contract = electricalContract;
        this.collectorMotor = sparkMaxFactory.createWithoutProperties(contract.getCollectorMotor(), getPrefix(), "CollectorMotor");
        this.inControlNoteSensor = xDigitalInputFactory.create(contract.getInControlNoteSensorDio());
        this.readyToFireNoteSensor = xDigitalInputFactory.create(contract.getReadyToFireNoteSensorDio());

        pf.setPrefix(this);
        intakePower = pf.createPersistentProperty("intakePower",0.1);
        ejectPower = pf.createPersistentProperty("ejectPower",-0.1);
        firePower = pf.createPersistentProperty("firePower", 1.0);
        intakePowerInControlMultiplier = pf.createPersistentProperty("intakePowerMultiplier", 0.5);
        this.intakeState = IntakeState.STOPPED;
    }

    public IntakeState getIntakeState(){
        return intakeState;
    }
    public void intake(){
        double power = intakePower.get();
        if (getGamePieceInControl()) {
            power *= intakePowerInControlMultiplier.get();
        }
        collectorMotor.set(power);
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
    public void fire(){
        collectorMotor.set(firePower.get());
        intakeState = IntakeState.FIRING;
    }
    public boolean getGamePieceInControl() {
        return inControlNoteSensor.get();
    }

    public boolean getGamePieceReady() {
        return readyToFireNoteSensor.get();
    }

    @Override
    public void periodic() {
        aKitLog.record("HasGamePiece", getGamePieceReady());
    }

    @Override
    public void refreshDataFrame() {
        collectorMotor.refreshDataFrame();
        inControlNoteSensor.refreshDataFrame();
        readyToFireNoteSensor.refreshDataFrame();
    }
}