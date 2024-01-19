package competition.subsystems.collector;

import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSubsystem extends BaseSubsystem{
    public XCANSparkMax collectorMotor;
    public DoubleProperty intakePower;
    public DoubleProperty ejectPower;

    private IntakeState intakeState;

    private int loopcount;

    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED
    }

    @Inject
    public CollectorSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory){
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

    @Override
    public void periodic() {
        loopcount++;
    }
}