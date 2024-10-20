package competition.subsystems.shooter;

import com.revrobotics.CANSparkBase;

import competition.subsystems.oracle.ScoringLocation;
import competition.subsystems.pose.PointOfInterest;
import xbot.common.advantage.DataFrameRefreshable;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseSetpointSubsystem;
import competition.electrical_contract.ElectricalContract;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMaxPIDProperties;
import xbot.common.math.DoubleInterpolator;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class ShooterWheelSubsystem extends BaseSetpointSubsystem<ShooterWheelTargetSpeeds> implements DataFrameRefreshable {
    public enum TargetRPM {
        STOP,
        TYPICAL,
        MELEE,
        INTO_AMP,
        LOB_SHOT
    }

    //need pose for real time calculations
    PoseSubsystem pose;
    DoubleInterpolator converter;

    DoubleInterpolator upperWheelDistanceToRpmInterpolator;
    DoubleInterpolator lowerWheelDistanceToRpmInterpolator;


    // IMPORTANT PROPERTIES
    private ShooterWheelTargetSpeeds targetRpms = new ShooterWheelTargetSpeeds(0.0);
    private double trimRpm;
    private final DoubleProperty intoAmpShotRpm;
    private double iMaxAccumValueForShooter;
    private final DoubleProperty typicalShotRpm;
    private final DoubleProperty meleeShotRpm;
    private final DoubleProperty distanceShotRpm;
    private final DoubleProperty lobShotRpm;

    //DEFINING MOTORS
    public XCANSparkMax upperWheelMotor;
    public XCANSparkMax lowerWheelMotor;

    // DEFINING CONTRACT
    final ElectricalContract contract;

    @Inject
    public ShooterWheelSubsystem(XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory, PropertyFactory pf, ElectricalContract contract, PoseSubsystem pose) {
        log.info("Creating ShooterWheelSubsystem");
        this.contract = contract;
        pf.setPrefix(this);

        typicalShotRpm = pf.createPersistentProperty("TypicalShotRpm", 4000);
        meleeShotRpm = pf.createPersistentProperty("MeleeShotRpm", 3600);
        intoAmpShotRpm = pf.createPersistentProperty("IntoAmpShotRpm", 800);
        distanceShotRpm = pf.createPersistentProperty("DistanceShotRpm", 4800);
        //Value still needs to be found
        lobShotRpm = pf.createPersistentProperty("LobShotRpm", 3800);

        this.pose = pose;
        this.converter = new DoubleInterpolator();

        // NEEDS TUNING TO FIND CORRECT VALUE
        iMaxAccumValueForShooter = 0;

        XCANSparkMaxPIDProperties defaultShooterPidProperties = new XCANSparkMaxPIDProperties(
                0.00015,
                0.0000005,
                0.0,
                300.0,
                0.00019,
                1,
                -1
        );

        if (contract.isShooterReady()) {
            this.upperWheelMotor = sparkMaxFactory.create(contract.getShooterMotorLeader(), this.getPrefix(),
                    "ShooterMaster", "ShooterWheel", defaultShooterPidProperties);
            this.lowerWheelMotor = sparkMaxFactory.create(contract.getShooterMotorFollower(), this.getPrefix(),
                    "ShooterFollower", "ShooterWheel", defaultShooterPidProperties);

            upperWheelMotor.setIdleMode(CANSparkBase.IdleMode.kCoast);
            lowerWheelMotor.setIdleMode(CANSparkBase.IdleMode.kCoast);

            upperWheelMotor.setSmartCurrentLimit(60);
            lowerWheelMotor.setSmartCurrentLimit(60);

            upperWheelMotor.setMeasurementPeriod(8);
            lowerWheelMotor.setMeasurementPeriod(8);

            upperWheelMotor.setAverageDepth(1);
            lowerWheelMotor.setAverageDepth(1);
        }

        // distance in meters
        var distanceArray =      new double[]{0.00, 1.52, 2.97, 3.92, 4.28, 4.56, 4.68, 5.55};
        var upperWheelRPMArray = new double[]{3600, 3600, 4000, 4500, 4500, 4500, 4800, 4800};
        var lowerWheelRPMArray = new double[]{3600, 3600, 4000, 4500, 4500, 4500, 4800, 4800};

        upperWheelDistanceToRpmInterpolator = new DoubleInterpolator(distanceArray, upperWheelRPMArray);
        lowerWheelDistanceToRpmInterpolator = new DoubleInterpolator(distanceArray, lowerWheelRPMArray);
    }

    public void setTargetRPM(TargetRPM target) {
        switch (target) {
            case STOP -> setTargetValue(0.0);
            case TYPICAL -> setTargetValue(typicalShotRpm.get());
            case MELEE -> setTargetValue(meleeShotRpm.get());
            case INTO_AMP -> setTargetValue(intoAmpShotRpm.get());
            case LOB_SHOT ->  setTargetValue(lobShotRpm.get());
            default -> setTargetValue(0.0);
        }
    }

    public double getRPMForGivenScoringLocation(PointOfInterest pointOfInterest) {
        switch (pointOfInterest) {
            // These speeds may be different someday.
            case SubwooferTopScoringLocation, SubwooferMiddleScoringLocation, SubwooferBottomScoringLocation -> {return meleeShotRpm.get();}
            case PodiumScoringLocation, AmpFarScoringLocation, BottomSpikeCloserToSpeakerScoringLocation,
                    SpikeMiddle, TopSpikeCloserToSpeakerScoringLocation, OneRobotAwayFromCenterSubwooferScoringLocation,
                    TopSpikeScoringLocation -> {return typicalShotRpm.get();}
            case WingScoringLocation -> {return distanceShotRpm.get();}
            default -> {return 4000;}
        }
    }

    public void changeTrimRPM(double changeRate) {
        trimRpm = (getTrimRPM() + changeRate);
    }

    public void setTargetTrimRPM(double trim) {
        trimRpm = trim;
    }

    public double getTrimRPM() {
        return trimRpm;
    }

    public void resetTrimRPM() {
        trimRpm = 0;
    }

    @Override
    public ShooterWheelTargetSpeeds getCurrentValue() {
        //We want the actual current value from the motor not from the code
        if (contract.isShooterReady()) {
            return new ShooterWheelTargetSpeeds(upperWheelMotor.getVelocity(), lowerWheelMotor.getVelocity());
        }
        // DON'T RETURN NULL, OR ROBOT COULD POTENTIALLY CRASH, 0.0 IS SAFER
        return new ShooterWheelTargetSpeeds();
    }

    @Override
    public ShooterWheelTargetSpeeds getTargetValue() {
        // Include the trim RPM when reporting out the target RPM
        return new ShooterWheelTargetSpeeds(
                targetRpms.upperWheelsTargetRPM + getTrimRPM(),
                targetRpms.lowerWheelsTargetRPM + getTrimRPM());
    }

    @Override
    public void setTargetValue(ShooterWheelTargetSpeeds value) {
        targetRpms = value;
    }

    public void setTargetValue(double value) {
        setTargetValue(new ShooterWheelTargetSpeeds(value));
    }

    @Override
    public void setPower(ShooterWheelTargetSpeeds power) {
        if (contract.isShooterReady()) {
            upperWheelMotor.set(power.upperWheelsTargetRPM);
            lowerWheelMotor.set(power.upperWheelsTargetRPM);
        }
    }

    @Override
    public boolean isCalibrated() {
        return true;
    }

    public void resetWheel() {
        setPower(new ShooterWheelTargetSpeeds(0.0));
        setTargetValue(new ShooterWheelTargetSpeeds(0.0));
        resetPID();
    }

    public void stopWheel() {
        setPower(new ShooterWheelTargetSpeeds(0.0));
    }
    
    public void resetPID() {
        if (contract.isShooterReady()) {
            upperWheelMotor.setIAccum(0);
        }
    }

    //WAY TO SET THE ACTUAL PID
    public void setPidSetpoints(ShooterWheelTargetSpeeds speeds) {
        if (contract.isShooterReady()) {
            upperWheelMotor.setReference(speeds.upperWheelsTargetRPM, CANSparkBase.ControlType.kVelocity);
            lowerWheelMotor.setReference(speeds.lowerWheelsTargetRPM, CANSparkBase.ControlType.kVelocity);
        }
    }

    public void configurePID() {
        if (contract.isShooterReady()) {
            upperWheelMotor.setIMaxAccum(iMaxAccumValueForShooter, 0);
        }
    }

    //returns the RPM based on the distance from the speaker
    public ShooterWheelTargetSpeeds getSpeedForRange(){
        double distanceFromSpeaker = pose.getDistanceFromSpeaker();
        return new ShooterWheelTargetSpeeds(
                upperWheelDistanceToRpmInterpolator.getInterpolatedOutputVariable(distanceFromSpeaker),
                lowerWheelDistanceToRpmInterpolator.getInterpolatedOutputVariable(distanceFromSpeaker)
        );
    }

    public boolean hasNonIdleTarget() {
        return getTargetValue().upperWheelsTargetRPM > 50 || getTargetValue().lowerWheelsTargetRPM > 50;
    }

    public boolean isReadyToFire() {
        return isMaintainerAtGoal() && hasNonIdleTarget();
    }

    @Override
    protected boolean areTwoTargetsEquivalent(ShooterWheelTargetSpeeds target1, ShooterWheelTargetSpeeds target2) {
        return BaseSetpointSubsystem.areTwoDoublesEquivalent(target1.upperWheelsTargetRPM, target2.upperWheelsTargetRPM, 100)
                && BaseSetpointSubsystem.areTwoDoublesEquivalent(target1.lowerWheelsTargetRPM, target2.lowerWheelsTargetRPM, 100);
    }

    public void periodic() {
        if (contract.isShooterReady()) {
            upperWheelMotor.periodic();
            lowerWheelMotor.periodic();
        }

        aKitLog.record("TargetRPM", getTargetValue());
        aKitLog.record("CurrentRPM", getCurrentValue());
        aKitLog.record("TrimRPM", getTrimRPM());
    }

    public void refreshDataFrame() {
        if (contract.isShooterReady()) {
            upperWheelMotor.refreshDataFrame();
            lowerWheelMotor.refreshDataFrame();
        }
    }
}

