package competition.subsystems.collector;

import com.revrobotics.CANSparkBase;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.flipper.FlipperSubsystem;
import competition.subsystems.oracle.NoteCollectionInfoSource;
import competition.subsystems.oracle.NoteFiringInfoSource;
import competition.subsystems.shooter.ShooterWheelTargetSpeeds;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMaxPIDProperties;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.controls.sensors.XTimer;
import xbot.common.logic.TimeStableValidator;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSubsystem extends BaseSubsystem implements DataFrameRefreshable, NoteCollectionInfoSource, NoteFiringInfoSource {

    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED,
        FIRING
    }

    public enum CollectionSubstate {
        EvaluationNeeded,
        EagerCollection,
        TripwireHit,
        AggresivelyPauseCollection,
        MoveNoteCarefullyToReadyPosition,
        BeamBreakCollection,
        Complete
    }

    public final XCANSparkMax collectorMotor;
    public final DoubleProperty intakeSpeed;
    public final DoubleProperty beamBreakIntakeSpeed;
    private IntakeState intakeState;
    private CollectionSubstate collectionSubstate;
    public final XDigitalInput inControlNoteSensor;
    public final XDigitalInput readyToFireNoteSensor;
    public final XDigitalInput beamBreakSensor;
    private final ElectricalContract contract;
    private final DoubleProperty firePower;
    private final TimeStableValidator noteInControlValidator;
    double lastFiredTime = -Double.MAX_VALUE;
    final DoubleProperty waitTimeAfterFiring;
    boolean lowerTripwireHit = false;
    boolean upperTripwireHit = false;
    double timeOfLastNoteSensorTriggered = 0;
    final DoubleProperty carefulAdvanceSpeed;
    final DoubleProperty carefulAdvanceTimeout;
    final DoubleProperty lightToleranceTimeInterval;
    double carefulAdvanceBeginTime = -Double.MAX_VALUE;

    FlipperSubsystem flipper;

    double currentTargetSpeed = 0;


    @Inject
    public CollectorSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                              ElectricalContract electricalContract, XDigitalInput.XDigitalInputFactory xDigitalInputFactory,
                              FlipperSubsystem flipper) {
        this.contract = electricalContract;
        if (contract.isCollectorReady()) {
            this.collectorMotor = sparkMaxFactory.create(contract.getCollectorMotor(), getPrefix(), "CollectorMotor",
                    "CollectorMotor", new XCANSparkMaxPIDProperties(
                            0.0,
                            0.0,
                            0.0,
                            0.0,
                            0.000208,
                            1,
                            -1));
            collectorMotor.setSmartCurrentLimit(40);
            collectorMotor.setIdleMode(CANSparkBase.IdleMode.kCoast);
            collectorMotor.enableVoltageCompensation(12.0);

            collectorMotor.setMeasurementPeriod(8);
            collectorMotor.setAverageDepth(1);
        } else {
            this.collectorMotor = null;
        }

        this.inControlNoteSensor = xDigitalInputFactory.create(contract.getLowerNoteSensorDio(), this.getPrefix());
        this.readyToFireNoteSensor = xDigitalInputFactory.create(contract.getUpperNoteSensorDio(), this.getPrefix());
        this.beamBreakSensor = xDigitalInputFactory.create(contract.getBeamBreakSensorDio(), this.getPrefix());

        pf.setPrefix(this);
        intakeSpeed = pf.createPersistentProperty("intakeSpeed", 6000);
        beamBreakIntakeSpeed = pf.createPersistentProperty("beamBreakIntakeSpeed", 1000);

        firePower = pf.createPersistentProperty("firePower", 1.0);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        waitTimeAfterFiring = pf.createPersistentProperty("WaitTimeAfterFiring", 0.25);
        carefulAdvanceSpeed = pf.createPersistentProperty("CarefulAdvanceSpeed", 500);
        carefulAdvanceTimeout = pf.createPersistentProperty("CarefulAdvanceTimeout", 0.5);
        lightToleranceTimeInterval = pf.createPersistentProperty("toleranceTimeInterval", 1);

        this.intakeState = IntakeState.STOPPED;

        noteInControlValidator = new TimeStableValidator(() -> 0.1); // Checks for having the note over 0.1 seconds

        this.flipper = flipper;
    }

    public void resetCollectionState() {
        log.info("Resetting collection state.");
        collectionSubstate = CollectionSubstate.EvaluationNeeded;
        lowerTripwireHit = false;
        upperTripwireHit = false;
        carefulAdvanceBeginTime = -Double.MAX_VALUE;
    }

    public IntakeState getIntakeState(){
        return intakeState;
    }
    public void intake(){
        if (shouldCommitToFiring()){
            return;
        }

        double suggestedSpeed = 0;

        // When just starting collection cold, we need to check if any sensors are pressed
        // before figuring out what to do.
        if (collectionSubstate == CollectionSubstate.EvaluationNeeded) {
            if (getGamePieceReady()) {
                collectionSubstate = CollectionSubstate.Complete;
            } else if (getGamePieceInControl()) {
                collectionSubstate = CollectionSubstate.MoveNoteCarefullyToReadyPosition;
            } else if (getBeamBreakSensorActivated()) {
                collectionSubstate = CollectionSubstate.BeamBreakCollection;
            } else {
                collectionSubstate = CollectionSubstate.EagerCollection;
            }
        }

        // If we're in the clear, go ahead and start collecting.
        if (collectionSubstate == CollectionSubstate.EagerCollection) {
            if (getBeamBreakSensorActivated()) {
                collectionSubstate = CollectionSubstate.BeamBreakCollection;
            }

            // Keeping this part in here as well in case if we somehow
            // Skipped the BeamBreak sensor while collecting, which if so...
            // Means that collection over-shooting issues still exist :(
            if (getGamePieceInControl() || getGamePieceReady()) {
                lowerTripwireHit = getGamePieceInControl();
                upperTripwireHit = getGamePieceReady();
                collectionSubstate = CollectionSubstate.MoveNoteCarefullyToReadyPosition;
            } else {
                suggestedSpeed = intakeSpeed.get();
            }
        }

        if (collectionSubstate == CollectionSubstate.BeamBreakCollection) {
            if (getGamePieceInControl() || getGamePieceReady()) {
                lowerTripwireHit = getGamePieceInControl();
                upperTripwireHit = getGamePieceReady();
                carefulAdvanceBeginTime = XTimer.getFPGATimestamp();
                collectionSubstate = CollectionSubstate.MoveNoteCarefullyToReadyPosition;
            } else {
                suggestedSpeed = beamBreakIntakeSpeed.get();
            }
        }

        // Now to carefully advance the note to the ready position. This may involve running the intake backwards
        // if we went past the ready to fire position.
        if (collectionSubstate == CollectionSubstate.MoveNoteCarefullyToReadyPosition) {
            if (getGamePieceInControl()) {
                // We've backdriven hard enough to see the note again. No need to go further.
                lowerTripwireHit = true;
                upperTripwireHit = false;
            }

            if (getGamePieceReady()) {
                // The note is where it needs to be. We're done.
                collectionSubstate = CollectionSubstate.Complete;
            } else {
                if (lowerTripwireHit) {
                    suggestedSpeed = carefulAdvanceSpeed.get();
                }
                if (upperTripwireHit) {
                    // If the note hit the upper sensor, and we can't see it now,
                    // try driving backwards until we do
                    suggestedSpeed = -carefulAdvanceSpeed.get();
                }

                if (XTimer.getFPGATimestamp() - carefulAdvanceBeginTime > carefulAdvanceTimeout.get()) {
                    // If we've been trying to advance the note for a while, and it's not working,
                    // we need to stop and re-evaluate.
                    collectionSubstate = CollectionSubstate.EvaluationNeeded;
                }
            }
        }

        aKitLog.record("CollectionSubstate", collectionSubstate);
        aKitLog.record("LowerTripwireHit", lowerTripwireHit);
        aKitLog.record("UpperTripwireHit", upperTripwireHit);

        setTargetSpeed(suggestedSpeed);
        intakeState = IntakeState.INTAKING;
    }
    public void eject(){
        if (shouldCommitToFiring()){
            return;
        }

        setTargetSpeed(-intakeSpeed.get());
        intakeState = IntakeState.EJECTING;
    }
    public void stop(){
        if (shouldCommitToFiring()){
            return;
        }
        setTargetSpeed(0.0);
        setPower(0.0);
        resetPID();
        intakeState = IntakeState.STOPPED;
    }

    public void resetPID() {
        if (contract.isCollectorReady()) {
            collectorMotor.setIAccum(0);
        }
    }

    public void fire(){
        setPower(firePower.get());
        if (intakeState != IntakeState.FIRING) {
            lastFiredTime = XTimer.getFPGATimestamp();
        }
        intakeState = IntakeState.FIRING;
        timeOfLastNoteSensorTriggered = 0;
    }

    public double getSecondsSinceFiringBegan() {
        if (intakeState != IntakeState.FIRING) {
            return 0;
        }
        return XTimer.getFPGATimestamp() - lastFiredTime;
    }

    public boolean getGamePieceInControl() {
        if (contract.isCollectorReady()) {
            return inControlNoteSensor.get();
        }
        return false;
    }

    public boolean getGamePieceReady() {
        if (contract.isCollectorReady()) {
            return readyToFireNoteSensor.get();
        }
        return false;
    }

    public boolean getBeamBreakSensorActivated() {
        if (contract.isCollectorReady()) {
            return beamBreakSensor.get();
        }
        return false;
    }

    public boolean checkSensorForLights() {
        if (getBeamBreakSensorActivated() || getGamePieceInControl() || getGamePieceReady()) {
            timeOfLastNoteSensorTriggered = XTimer.getFPGATimestamp();
        }
        else {
            if (XTimer.getFPGATimestamp() - timeOfLastNoteSensorTriggered > lightToleranceTimeInterval.get()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean confidentlyHasControlOfNote() {
        return noteInControlValidator.peekStable();
    }

    @Override
    public boolean confidentlyHasFiredNote() {
        return getSecondsSinceFiringBegan() > waitTimeAfterFiring.get();
    }

    /**
     * If we start firing for any reason, we have to commit to running the intake at full power
     * for a few moments to avoid jamming the system.
     * @return Returns true if we have started, but not finished, the firing process.
     */
    public boolean shouldCommitToFiring() {
        return intakeState == IntakeState.FIRING && !confidentlyHasFiredNote();
    }

    @Override
    public void periodic() {
        if (contract.isCollectorReady()) {
            collectorMotor.periodic();
            noteInControlValidator.checkStable(getGamePieceInControl() || getGamePieceReady()
                    || getBeamBreakSensorActivated());

            aKitLog.record("GamePieceReady", getGamePieceReady());
            aKitLog.record("GamePieceInControl", getGamePieceInControl());
            aKitLog.record("ConfidentlyHasControlOfNote", confidentlyHasControlOfNote());
            aKitLog.record("ConfidentlyHasFiredNote", confidentlyHasFiredNote());
            aKitLog.record("IntakeState", intakeState);
            aKitLog.record("TargetSpeed", currentTargetSpeed);
            aKitLog.record("CollectorMotorVelocity", collectorMotor.getVelocity());
        }

    }

    @Override
    public void refreshDataFrame() {
        if (contract.isCollectorReady()) {
            collectorMotor.refreshDataFrame();
            inControlNoteSensor.refreshDataFrame();
            beamBreakSensor.refreshDataFrame();
            readyToFireNoteSensor.refreshDataFrame();
        }
    }

    
    public Double getTargetValue() {
        return currentTargetSpeed;
    }

    public void setTargetSpeed(Double value) {
        if (flipper.getActive()) {
            setPower(0.0);
        }
        else {
            currentTargetSpeed = value;
            if (contract.isCollectorReady()) {
                collectorMotor.setReference(value, CANSparkBase.ControlType.kVelocity);
            }
        }
    }

    public void setPower(Double power) {
        // not used for speed controller based systems
        if(contract.isCollectorReady()) {
            collectorMotor.set(power);
        }
    }
}