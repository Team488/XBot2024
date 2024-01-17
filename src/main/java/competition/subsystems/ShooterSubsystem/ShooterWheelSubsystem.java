package competition.subsystems.ShooterSubsystem;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMaxPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Singleton;


@Singleton
public class ShooterWheelSubsystem extends BaseSetpointSubsystem {
    private final DoubleProperty targetRpmProp;
    private final DoubleProperty currentRpmProp;
    private final DoubleProperty rpmTrimProp;
    private final DoubleProperty safeRpm;
    private final DoubleProperty nearShotRpm;
    private final DoubleProperty distanceShotRpm;
    private final DoubleProperty shortRangeErrorTolerance;
    private final DoubleProperty longRangeErrorTolerance;
    public XCANSparkMax leader;
    public XCANSparkMax follower;
    ElectricalContract contract;

    public enum TargetRPM {
        SAFE,
        NEARSHOT,
        DISTANCESHOT
    }


    public ShooterWheelSubsystem(XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory, PropertyFactory pf, ElectricalContract contract) {
        log.info("Creating ShooterWheelSubsystem");
        this.contract = contract;

        targetRpmProp = pf.createEphemeralProperty("TargetRPM", 0);
        currentRpmProp = pf.createEphemeralProperty("CurrentRPM", 0);
        rpmTrimProp = pf.createEphemeralProperty("TrimRPM", 0);

        safeRpm = pf.createPersistentProperty("SafeRpm", 500);
        nearShotRpm = pf.createPersistentProperty("NearShotRpm", 1000);
        distanceShotRpm = pf.createPersistentProperty("DistanceShotRpm", 3000);

        shortRangeErrorTolerance = pf.createPersistentProperty("ShortRangeErrorTolerance", 200);
        longRangeErrorTolerance = pf.createPersistentProperty("LongRangeErrorTolerance", 50);

//        XCANSparkMaxPIDProperties wheelDefaultProps = new XCANSparkMaxPIDProperties();
//        wheelDefaultProps.p = 0.00008;
//        wheelDefaultProps.i = 0;
//        wheelDefaultProps.d = 0;
//        wheelDefaultProps.feedForward = 0.000185;
//        wheelDefaultProps.iZone = 200;
//        wheelDefaultProps.maxOutput = 1;
//        wheelDefaultProps.minOutput = -1;

        if (contract.isShooterReady()) {

        }

    }

    public double getShortRangeErrorTolerance() {
        return shortRangeErrorTolerance.get();
    }

    public double getLongRangeErrorTolerance() {
        return longRangeErrorTolerance.get();
    }

    public void setTargetRPM(TargetRPM target) {
        switch (target) {
            case SAFE -> setTargetRPM(safeRpm.get());
            case NEARSHOT -> setTargetRPM(nearShotRpm.get());
            case DISTANCESHOT -> setTargetRPM(distanceShotRpm.get());
            default -> setTargetRPM(0);
        }
    }

    public void setTargetRPM(double speed) {
        targetRpmProp.set(speed);
    }




}
