package competition.subsystems.ShooterSubsystem;

import com.revrobotics.REVLibError;
import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMaxPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Singleton;


@Singleton
public class ShooterWheelSubsystem extends BaseSetpointSubsystem<Double> {
    private final DoubleProperty targetRpmProp;
    private final DoubleProperty currentRpmProp;
    private final DoubleProperty rpmTrimProp;
    private final DoubleProperty safeRpm;
    private final DoubleProperty nearShotRpm;
    private final DoubleProperty distanceShotRpm;
    public XCANSparkMax leader;
    public XCANSparkMax follower;
    ElectricalContract contract;

    @Override
    public Double getCurrentValue() {
        return leader.getVelocity();
    }

    // WE

    @Override
    public Double getTargetValue() {
        return targetRpmProp.get();
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
        return true;
    }

    public enum TargetRPM {
        SAFE,
        NEARSHOT,
        DISTANCESHOT
    }

    public ShooterWheelSubsystem(XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory, PropertyFactory pf, ElectricalContract contract) {
        log.info("Creating ShooterWheelSubsystem");
        this.contract = contract;

        targetRpmProp = pf.createEphemeralProperty("TargetRPM", 0);
        currentRpmProp = pf.createEphemeralProperty("CurrentRPM", 500);
        rpmTrimProp = pf.createEphemeralProperty("TrimRPM", 0);

        safeRpm = pf.createPersistentProperty("SafeRpm", 500);
        nearShotRpm = pf.createPersistentProperty("NearShotRpm", 1000);
        distanceShotRpm = pf.createPersistentProperty("DistanceShotRpm", 3000);


        // MOTOR RELATED
        XCANSparkMaxPIDProperties wheelDefaultProps = new XCANSparkMaxPIDProperties();
        wheelDefaultProps.p = 0.00008;
        wheelDefaultProps.i = 0;
        wheelDefaultProps.d = 0;
        wheelDefaultProps.feedForward = 0.000185;
        wheelDefaultProps.iZone = 200;
        wheelDefaultProps.maxOutput = 1;
        wheelDefaultProps.minOutput = -1;

        if (contract.isShooterReady()) {
            this.leader = sparkMaxFactory.create(contract.getShooterMotorLeader(), this.getPrefix(),
                    "ShooterMaster", wheelDefaultProps);
            this.follower = sparkMaxFactory.create(contract.getShooterMotorFollower(), this.getPrefix(),
                    "ShooterFollower");
            this.follower.follow(this.leader, true);

        }
    }

    public void setTargetRPM(TargetRPM target) {
        switch (target) {
            case SAFE -> setTargetValue(safeRpm.get());
            case NEARSHOT -> setTargetValue(nearShotRpm.get());
            case DISTANCESHOT -> setTargetValue(distanceShotRpm.get());
            default -> setTargetValue(0);
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

    public double getCurrentRPM() {
        return currentRpmProp.get();
    }

    public double getTargetRPM() {
        return targetRpmProp.get();
    }




}
