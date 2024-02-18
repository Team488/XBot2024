package competition.subsystems.shooter;

import com.revrobotics.CANSparkBase;
import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.Logger;

import xbot.common.advantage.DataFrameRefreshable;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseSetpointSubsystem;
import competition.electrical_contract.ElectricalContract;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMaxPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class ShooterWheelSubsystem extends BaseSetpointSubsystem<Double> implements DataFrameRefreshable {
    public enum TargetRPM {
        SAFE,
        NEARSHOT,
        DISTANCESHOT,
        AMP_SHOT
    }

    //need pose for real time calculations
    PoseSubsystem pose;
    ShooterDistanceToRpmConverter converter;


    // IMPORTANT PROPERTIES
    private double targetRpm;
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
        // placeholder value for rpm when preparing to score in amp
        ampShotRpm = pf.createPersistentProperty("AmpShotRpm", 2000);

        this.pose = pose;
        this.converter = new ShooterDistanceToRpmConverter();


        // WE WON'T BE NEEDING THESE AS CURRENTLY WE ARE USING A UNIVERSAL ERROR TOLERANCE "acceptableToleranceRPM"
        shortRangeErrorToleranceRpm = pf.createPersistentProperty("ShortRangeErrorTolerance", 300);
        longRangeErrorToleranceRpm = pf.createPersistentProperty("LongRangeErrorTolerance", 100);

        // NEEDS TUNING TO FIND CORRECT VALUE
        iMaxAccumValueForShooter = pf.createPersistentProperty("IMaxAccumValueForShooter", 0);


        // THIS IS HOW MUCH RPM WE CAN TOLERATE (needs testing and is UNIVERSAL)
        acceptableToleranceRPM = pf.createPersistentProperty("AcceptableToleranceRPM", 200);

        XCANSparkMaxPIDProperties defaultShooterPidProperties = new XCANSparkMaxPIDProperties(
                0.0007,
                0.0,
                0.0,
                0.0,
                0.00019,
                1,
                -1
        );

        if (contract.isShooterReady()) {
            this.leader = sparkMaxFactory.create(contract.getShooterMotorLeader(), this.getPrefix(),
                    "ShooterMaster", "ShooterWheel", defaultShooterPidProperties);
            this.follower = sparkMaxFactory.createWithoutProperties(contract.getShooterMotorFollower(), this.getPrefix(),
                    "ShooterFollower");
            this.follower.follow(this.leader, false);

            this.leader.setP(defaultShooterPidProperties.p());
            this.leader.setFF(defaultShooterPidProperties.feedForward());
        }
    }

    public void setTargetRPM(TargetRPM target) {
        switch (target) {
            case SAFE -> setTargetValue(safeRpm.get());
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
        log.info("Target RPM: " + value);
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

    public void resetWheel() {
        setPower(0.0);
        setTargetValue(0.0);
        resetPID();
    }

    public void stopWheel() {
        setPower(0.0);
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
            leader.setIAccum(0);
        }
    }

    //WAY TO SET THE ACTUAL PID
    public void setPidSetpoint(double speed) {
        if (contract.isShooterReady()) {
            leader.setReference(speed, CANSparkBase.ControlType.kVelocity);
        }
    }

    public void configurePID() {
        if (contract.isShooterReady()) {
            leader.setIMaxAccum(iMaxAccumValueForShooter.get(), 0);
        }
    }
    public void periodic() {
        leader.periodic();
        follower.periodic();

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
        return converter.getRPMForDistance(pose.getDistanceFromSpeaker());
    }
}

