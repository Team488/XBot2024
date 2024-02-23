package competition.subsystems.collector;

import com.revrobotics.CANSparkBase;
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
        if (contract.isCollectorReady()) {
            this.collectorMotor = sparkMaxFactory.createWithoutProperties(contract.getCollectorMotor(), getPrefix(), "CollectorMotor");
            collectorMotor.setSmartCurrentLimit(40);
            collectorMotor.setIdleMode(CANSparkBase.IdleMode.kCoast);
        } else {
            this.collectorMotor = null;
        }

        this.inControlNoteSensor = xDigitalInputFactory.create(contract.getInControlNoteSensorDio(), this.getPrefix());
        this.readyToFireNoteSensor = xDigitalInputFactory.create(contract.getReadyToFireNoteSensorDio(), this.getPrefix());

        pf.setPrefix(this);
        intakePower = pf.createPersistentProperty("intakePower",0.8);
        ejectPower = pf.createPersistentProperty("ejectPower",-0.8);
        firePower = pf.createPersistentProperty("firePower", 1.0);
        intakePowerInControlMultiplier = pf.createPersistentProperty("intakePowerMultiplier", 1.0);
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
        if (getGamePieceReady()) {
            power = 0;
        }
        setPower(power);
        intakeState = IntakeState.INTAKING;
    }
    public void eject(){
        setPower(ejectPower.get());
        intakeState = IntakeState.EJECTING;
    }
    public void stop(){
        setPower(0);
        intakeState = IntakeState.STOPPED;
    }
    public void fire(){
        setPower(firePower.get());
        intakeState = IntakeState.FIRING;
    }

    public void setPower(double power) {
        if (contract.isCollectorReady()) {
            collectorMotor.set(power);
        }
    }

    public boolean getGamePieceInControl() {
        if (contract.isCollectorReady()) {
            return !inControlNoteSensor.get();
        }
        return false;
    }

    public boolean getGamePieceReady() {
        if (contract.isCollectorReady()) {
            return !readyToFireNoteSensor.get();
        }
        return false;
    }

    @Override
    public void periodic() {
        if (contract.isCollectorReady()) {
            aKitLog.record("GamePieceReady", getGamePieceReady());
            aKitLog.record("GamePieceInControl", getGamePieceInControl());
        }
    }

    @Override
    public void refreshDataFrame() {
        if (contract.isCollectorReady()) {
            collectorMotor.refreshDataFrame();
            inControlNoteSensor.refreshDataFrame();
            readyToFireNoteSensor.refreshDataFrame();
        }
    }
}