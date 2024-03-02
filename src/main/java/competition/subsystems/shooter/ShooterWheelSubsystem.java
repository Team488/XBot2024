package competition.subsystems.shooter;

import com.revrobotics.CANSparkBase;

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
        SUBWOOFER,
        NEARSHOT,
        DISTANCESHOT,
        AMP_SHOT
    }

    //need pose for real time calculations
    PoseSubsystem pose;
    DoubleInterpolator converter;

    DoubleInterpolator upperWheelDistanceToRpmInterpolator;
    DoubleInterpolator lowerWheelDistanceToRpmInterpolator;


    // IMPORTANT PROPERTIES
    private ShooterWheelTargetSpeeds targetRpms = new ShooterWheelTargetSpeeds(0.0);
    private double trimRpm;
    private final DoubleProperty safeRpm;
    private final DoubleProperty nearShotRpm;
    private final DoubleProperty distanceShotRpm;
    private final DoubleProperty ampShotRpm;
    private final DoubleProperty shortRangeErrorToleranceRpm;
    private final DoubleProperty longRangeErrorToleranceRpm;
    private final DoubleProperty iMaxAccumValueForShooter;
    private final DoubleProperty acceptableToleranceRPM;

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

        safeRpm = pf.createPersistentProperty("SafeRpm", 500);
        nearShotRpm = pf.createPersistentProperty("NearShotRpm", 1000);
        distanceShotRpm = pf.createPersistentProperty("DistanceShotRpm", 3000);
        // placeholder value for rpm when preparing to score in amp
        ampShotRpm = pf.createPersistentProperty("AmpShotRpm", 2000);

        this.pose = pose;
        this.converter = new DoubleInterpolator();

        // WE WON'T BE NEEDING THESE AS CURRENTLY WE ARE USING A UNIVERSAL ERROR TOLERANCE "acceptableToleranceRPM"
        shortRangeErrorToleranceRpm = pf.createPersistentProperty("ShortRangeErrorTolerance", 300);
        longRangeErrorToleranceRpm = pf.createPersistentProperty("LongRangeErrorTolerance", 100);

        // NEEDS TUNING TO FIND CORRECT VALUE
        iMaxAccumValueForShooter = pf.createPersistentProperty("IMaxAccumValueForShooter", 0);

        // THIS IS HOW MUCH RPM WE CAN TOLERATE (needs testing and is UNIVERSAL)
        acceptableToleranceRPM = pf.createPersistentProperty("AcceptableToleranceRPM", 200);

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
        }

        var distanceArray =      new double[]{0,    36,   49.5, 63,   80,   111,  136};
        var upperWheelRPMArray = new double[]{4000, 4000, 4000, 4000, 4000, 4000, 4500};
        var lowerWheelRPMArray = new double[]{4000, 4000, 4000, 4000, 4000, 4000, 4500};

        upperWheelDistanceToRpmInterpolator = new DoubleInterpolator(distanceArray, upperWheelRPMArray);
        lowerWheelDistanceToRpmInterpolator = new DoubleInterpolator(distanceArray, lowerWheelRPMArray);
    }

    public void setTargetRPM(TargetRPM target) {
        switch (target) {
            case STOP -> setTargetValue(0.0);
            case SUBWOOFER -> setTargetValue(safeRpm.get());
            case NEARSHOT -> setTargetValue(nearShotRpm.get());
            case DISTANCESHOT -> setTargetValue(distanceShotRpm.get());
            case AMP_SHOT -> setTargetValue(ampShotRpm.get());
            default -> setTargetValue(0.0);
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


    // GET ALL THE TOLERANCES
    public double getShortRangeErrorTolerance() {
        return shortRangeErrorToleranceRpm.get();
    }

    public double getLongRangeErrorTolerance() {
        return longRangeErrorToleranceRpm.get();
    }

    public double getAcceptableToleranceRPM() {
        return acceptableToleranceRPM.get();
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
            upperWheelMotor.setIMaxAccum(iMaxAccumValueForShooter.get(), 0);
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

