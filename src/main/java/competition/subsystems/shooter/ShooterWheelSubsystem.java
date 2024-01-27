package competition.subsystems.shooter;

import xbot.common.command.BaseSetpointSubsystem;
import competition.electrical_contract.ElectricalContract;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import javax.inject.Singleton;


@Singleton
public class ShooterWheelSubsystem extends BaseSetpointSubsystem<Double> {
    public enum TargetRPM {
        SAFE,
        NEARSHOT,
        DISTANCESHOT
    }

    // IMPORTANT PROPERTIES
    private final DoubleProperty targetRpmProp;
    private final DoubleProperty currentRpmProp;
    private final DoubleProperty rpmTrimProp;
    private final DoubleProperty safeRpm;
    private final DoubleProperty nearShotRpm;
    private final DoubleProperty distanceShotRpm;
    private final DoubleProperty shortRangeErrorTolerance;
    private final DoubleProperty longRangeErrorTolerance;

    //DEFINING MOTORS
    public XCANSparkMax leader;
    public XCANSparkMax follower;

    // DEFINING CONTRACT
    final ElectricalContract contract;

    public ShooterWheelSubsystem(XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory, PropertyFactory pf, ElectricalContract contract) {
        log.info("Creating ShooterWheelSubsystem");
        this.contract = contract;

        // EVERY VALUE SHOULD BE SET TO ZERO AT FIRST
        targetRpmProp = pf.createEphemeralProperty("TargetRPM", 0);
        currentRpmProp = pf.createEphemeralProperty("CurrentRPM", 0);
        rpmTrimProp = pf.createEphemeralProperty("TrimRPM", 0);

        safeRpm = pf.createPersistentProperty("SafeRpm", 500);
        nearShotRpm = pf.createPersistentProperty("NearShotRpm", 1000);
        distanceShotRpm = pf.createPersistentProperty("DistanceShotRpm", 3000);

        shortRangeErrorTolerance = pf.createPersistentProperty("ShortRangeErrorTolerance", 0);
        longRangeErrorTolerance = pf.createPersistentProperty("LongRangeErrorTolerance", 0);

        // MOTOR RELATED, COULD BE USED LATER
//        XCANSparkMaxPIDProperties wheelDefaultProps = new XCANSparkMaxPIDProperties();
//        wheelDefaultProps.p = 0.00008;
//        wheelDefaultProps.i = 0;
//        wheelDefaultProps.d = 0;
//        wheelDefaultProps.feedForward = 0.000185;
//        wheelDefaultProps.iZone = 200;
//        wheelDefaultProps.maxOutput = 1;
//        wheelDefaultProps.minOutput = -1;

        if (contract.isShooterReady()) {
            this.leader = sparkMaxFactory.create(contract.getShooterMotorLeader(), this.getPrefix(),
                    "ShooterMaster", null);
            this.follower = sparkMaxFactory.create(contract.getShooterMotorFollower(), this.getPrefix(),
                    "ShooterFollower", null);
            this.follower.follow(this.leader, true);

        }
    }

    public void setTargetRPM(TargetRPM target) {
        switch (target) {
            case SAFE -> setTargetValue(safeRpm.get());
            case NEARSHOT -> setTargetValue(nearShotRpm.get());
            case DISTANCESHOT -> setTargetValue(distanceShotRpm.get());
            default -> setTargetValue(0.0);
        }
    }

    public void changeTrimRPM(double changeRate) {
        rpmTrimProp.set(getTrimRPM() + changeRate);
    }

    public void setTargetTrimRPM(double trim) {
        rpmTrimProp.set(trim);
    }

    public double getTrimRPM() {
        return rpmTrimProp.get();
    }

    public void resetTrimRPM() {
        rpmTrimProp.set(0);
    }

    @Override
    public Double getCurrentValue() {
        //We want the actual current value from the motor not from the code
        if (contract.isShooterReady()) {
            return leader.getVelocity();
        }
        // DON'T RETURN NULL, OR ROBOT COULD POTENTIALLY CRASH, 0.0 IS SAFER
        return 0.0;
    }

    @Override
    public Double getTargetValue() {
        return targetRpmProp.get() + getTrimRPM();
    }

    @Override
    public void setTargetValue(Double value) {
        targetRpmProp.set(value);
    }

    @Override
    public void setPower(Double power) {
        if (contract.isShooterReady()) {
            leader.set(power);
        }
    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    public void resetWheel() {
        setPower((double) 0);
        setTargetValue((double) 0);
        resetPID();
    }

    public void stopWheel() {
        setPower((double) 0);
    }

    public double getShortRangeErrorTolerance() {
        return shortRangeErrorTolerance.get();
    }


    public double getLongRangeErrorTolerance() {
        return longRangeErrorTolerance.get();
    }

    public void resetPID() {
        if (contract.isShooterReady()) {
            leader.setIAccum(0);
        }
    }

    public void configurePID() {
        if (contract.isShooterReady()) {
            leader.setIMaxAccum(0, 0);
        }
    }
}
