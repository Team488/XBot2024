package competition.subsystems.shooter;

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

    @Override
    public void periodic() {
        Logger.recordOutput(getPrefix() + "TargetRPM", getTargetValue());
        Logger.recordOutput(getPrefix() + "CurrentRPM", getCurrentValue());
        Logger.recordOutput(getPrefix() + "TrimRPM", getTrimRPM());
    }

    @Override
    public void refreshDataFrame() {
        if (contract.isShooterReady()) {
            leader.refreshDataFrame();
            follower.refreshDataFrame();
        }
    }
    
    //returns the RPM based on the distance from the speaker
    public double getSpeedForRange(){
        double distanceFromSpeakerInMeters;
        switch(DriverStation.getAlliance().get()){

            //returns the distance from speaker in meters based on alliance multiplied by the ratio for RPM
            case Red -> {
                distanceFromSpeakerInMeters = pose.getCurrentPose2d().getTranslation().getDistance(PoseSubsystem.BLUE_SPEAKER_POSITION);
            }
            default -> {
                distanceFromSpeakerInMeters = pose.getCurrentPose2d().getTranslation().getDistance(PoseSubsystem.BLUE_SPEAKER_POSITION);
            }
        }
        //DISCLAIMER 400 IS JUST A PLACEHOLDER VALUE FOR METERS -> RPM RATIO, MORE TESTING IS REQUIRED TO FIGURE OUT THE CORRECT NUMBER
        double RPM = distanceFromSpeakerInMeters * 400;
        return RPM;
    }
}

