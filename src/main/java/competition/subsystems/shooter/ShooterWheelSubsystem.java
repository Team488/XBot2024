package competition.subsystems.shooter;


import competition.subsystems.vision.ShooterDistanceToRpmConverter;

import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.Logger;
import xbot.common.advantage.DataFrameRefreshable;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseSetpointSubsystem;
import competition.electrical_contract.ElectricalContract;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class ShooterWheelSubsystem extends BaseSetpointSubsystem<Double> implements DataFrameRefreshable {
    public enum TargetRPM {
        SAFE,
        NEARSHOT,
        DISTANCESHOT
    }

    //need pose for real time calculations
    PoseSubsystem pose;


    // IMPORTANT PROPERTIES
    private double targetRpm;
    private double trimRpm;
    private final DoubleProperty safeRpm;
    private final DoubleProperty nearShotRpm;
    private final DoubleProperty distanceShotRpm;
    private final DoubleProperty shortRangeErrorToleranceRpm;
    private final DoubleProperty longRangeErrorToleranceRpm;
    private final DoubleProperty iMaxAccumValueForShooter;






    //DEFINING MOTORS
    public XCANSparkMax leader;
    public XCANSparkMax follower;

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

        this.pose = pose;


        shortRangeErrorToleranceRpm = pf.createPersistentProperty("ShortRangeErrorTolerance", 300);
        longRangeErrorToleranceRpm = pf.createPersistentProperty("LongRangeErrorTolerance", 100);

        // NEEDS TUNING TO FIND CORRECT VALUE
        iMaxAccumValueForShooter = pf.createPersistentProperty("IMaxAccumValueForShooter", 0);


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
        return targetRpm + getTrimRPM();
    }

    @Override
    public void setTargetValue(Double value) {
        targetRpm = value;
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
        setPower(0.0);
        setTargetValue(0.0);
        resetPID();
    }

    public void stopWheel() {
        setPower(0.0);
    }

    public double getShortRangeErrorTolerance() {
        return shortRangeErrorToleranceRpm.get();
    }

    public double getLongRangeErrorTolerance() {
        return longRangeErrorToleranceRpm.get();
    }

    public void resetPID() {
        if (contract.isShooterReady()) {
            leader.setIAccum(0);
        }
    }

    public void configurePID() {
        if (contract.isShooterReady()) {
            leader.setIMaxAccum(iMaxAccumValueForShooter.get(), 0);
        }
    }
    public void periodic() {
        aKitLog.record("TargetRPM", getTargetValue());
        aKitLog.record("CurrentRPM", getCurrentValue());
        aKitLog.record("TrimRPM", getTrimRPM());
    }

    public void refreshDataFrame() {
        if (contract.isShooterReady()) {
            leader.refreshDataFrame();
            follower.refreshDataFrame();
        }
    }
    
    //returns the RPM based on the distance from the speaker
    public double getSpeedForRange(){
        double distanceFromSpeakerInMeters;

        distanceFromSpeakerInMeters = pose.getCurrentPose2d().getTranslation().getDistance(
                PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_POSITION));
        //DISCLAIMER 400 IS JUST A PLACEHOLDER VALUE FOR METERS -> RPM RATIO, MORE TESTING IS REQUIRED TO FIGURE OUT THE CORRECT NUMBER
        double rpm = distanceFromSpeakerInMeters * 400;

        return rpm;
    }
}

