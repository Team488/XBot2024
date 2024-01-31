package competition.subsystems.collector;

import competition.electrical_contract.ElectricalContract;
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
public class CollectorSubsystem extends BaseSubsystem{
    public final XCANSparkMax collectorMotor;
    public final DoubleProperty intakePower;
    public final DoubleProperty ejectPower;
    private IntakeState intakeState;
    private final BooleanProperty gamePieceCollected;
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
        this.collectorMotor = sparkMaxFactory.create(contract.getCollectorMotor(), getPrefix(), "CollectorMotor", null);
        this.noteSensor = xDigitalInputFactory.create(contract.getNoteSensorDio().channel);

        pf.setPrefix(this);
        intakePower = pf.createPersistentProperty("intakePower",0.1);
        ejectPower = pf.createPersistentProperty("ejectPower",0.1);
        gamePieceCollected = pf.createEphemeralProperty("HasGamePiece", false);

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
        return gamePieceCollected.get();
    }
    public void updateGamePieceCollected() {
        gamePieceCollected.set(noteSensor.get());
    }
    @Override
    public void periodic() {
        updateGamePieceCollected();
    }

}