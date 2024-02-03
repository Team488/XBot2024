package competition.subsystems.arm;

import com.revrobotics.SparkLimitSwitch;
import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.math.MathUtils;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ArmSubsystem extends BaseSetpointSubsystem<Double> implements DataFrameRefreshable {

    public XCANSparkMax armMotorLeft;
    public XCANSparkMax armMotorRight;

    public final ElectricalContract contract;

    public ArmState armState;

    public DoubleProperty extendPower;
    public DoubleProperty retractPower;

    private DoubleProperty armPowerMax;
    private DoubleProperty armPowerMin;


    public DoubleProperty ticksToMmRatio; // Millimeters
    public DoubleProperty armMotorLeftRevolutionOffset; // # of revolutions
    public DoubleProperty armMotorRightRevolutionOffset;
    public DoubleProperty armMotorRevolutionLimit;
    boolean hasCalibratedLeft;
    boolean hasCalibratedRight;

    private double targetAngle;

    public enum ArmState {
        EXTENDING,
        RETRACTING,
        STOPPED
    }

    public enum LimitState {
        UPPER_LIMIT_HIT,
        LOWER_LIMIT_HIT,
        NOT_AT_LIMIT,
        BOTH_LIMITS_HIT
    }

    public enum UsefulArmPosition {
        STARTING_POSITION,
        COLLECTING_FROM_GROUND,
        FIRING_FROM_SPEAKER_FRONT,
        FIRING_IN_AMP
    }

    public enum ArmNearLimitState {
        NEAR_UPPER_LIMIT,
        NEAR_LOWER_LIMIT,
        NOT_NEAR_LIMIT
    }

    @Inject
    public ArmSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                        ElectricalContract contract) {

        pf.setPrefix(this);
        this.contract = contract;

        extendPower = pf.createPersistentProperty("ExtendPower", 0.1);
        retractPower = pf.createPersistentProperty("RetractPower", 0.1);
      
        armPowerMax = pf.createPersistentProperty("ArmPowerMax", 0.5);
        armPowerMin = pf.createPersistentProperty("ArmPowerMin", -0.5);

        // All the DoubleProperties below needs configuration.
        ticksToMmRatio = pf.createPersistentProperty("TicksToDistanceRatio", 1000);

        armMotorLeftRevolutionOffset = pf.createPersistentProperty(
                "ArmMotorLeftRevolutionOffset", 0);
        armMotorRightRevolutionOffset = pf.createPersistentProperty(
                "ArmMotorRightRevolutionOffset", 0);

        armMotorRevolutionLimit = pf.createPersistentProperty("ArmMotorPositionLimit", 15000);
        hasCalibratedLeft = false;
        hasCalibratedRight = false;

        if (contract.isArmReady()) {
            armMotorLeft = sparkMaxFactory.createWithoutProperties(
                    contract.getArmMotorLeft(), this.getPrefix(), "ArmMotorLeft");
            armMotorRight = sparkMaxFactory.createWithoutProperties(
                    contract.getArmMotorRight(), this.getPrefix(), "ArmMotorRight");

            // Enable hardware limits
            armMotorLeft.setForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyClosed, true);
            armMotorLeft.setReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyClosed, true);
            armMotorRight.setForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyClosed, true);
            armMotorRight.setReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyClosed, true);
        }

        this.armState = ArmState.STOPPED;
    }


    public ArmNearLimitState checkIsArmNearLimit(double actualPosition) {
        if (actualPosition >= armMotorRevolutionLimit.get() * 0.85) {
            return ArmNearLimitState.NEAR_UPPER_LIMIT;
        } else if (actualPosition <= armMotorRevolutionLimit.get() * 0.15) {
            return ArmNearLimitState.NEAR_LOWER_LIMIT;
        }
        return ArmNearLimitState.NOT_NEAR_LIMIT;
    }

  
    public void setPowerToLeftAndRightArms(double leftPower, double rightPower) {
        // Check if armPowerMin/armPowerMax are safe values
        if (armPowerMax.get() < 0 || armPowerMin.get() > 0) {
            armMotorLeft.set(0);
            armMotorRight.set(0);
            log.error("armPowerMax or armPowerMin values out of bound!");
            return;
        }

        // If not calibrated, motor can only go down at slow rate
        if (!(hasCalibratedLeft && hasCalibratedRight)) {
            leftPower = MathUtils.constrainDouble(leftPower, -0.1, 0);
            rightPower = MathUtils.constrainDouble(leftPower, -0.1, 0);

        } else {
            // If calibrated, restrict movement to area
            ArmNearLimitState left = checkIsArmNearLimit(
                    armMotorLeft.getPosition() + armMotorLeftRevolutionOffset.get());
            ArmNearLimitState right = checkIsArmNearLimit(
                    armMotorRight.getPosition() + armMotorLeftRevolutionOffset.get());

            switch (left) {
                case NEAR_LOWER_LIMIT -> leftPower = MathUtils.constrainDouble(
                            leftPower, 0, armPowerMax.get());
                case NEAR_UPPER_LIMIT -> leftPower = MathUtils.constrainDouble(
                            leftPower, armPowerMin.get(), 0);
                default -> {}
            }

            switch (right) {
                case NEAR_LOWER_LIMIT -> rightPower = MathUtils.constrainDouble(
                        rightPower, 0, armPowerMax.get());
                case NEAR_UPPER_LIMIT -> rightPower = MathUtils.constrainDouble(
                        rightPower, armPowerMin.get(), 0);
                default -> {}
            }
        }
  
        // Arm at limit hit power restrictions
        switch(getLimitState(armMotorLeft)) {
            case BOTH_LIMITS_HIT -> leftPower = 0;
            case UPPER_LIMIT_HIT -> leftPower = MathUtils.constrainDouble(leftPower, armPowerMin.get(), 0);
            case LOWER_LIMIT_HIT -> leftPower = MathUtils.constrainDouble(leftPower, 0, armPowerMax.get());
            default -> {}
        }

        switch(getLimitState(armMotorRight)) {
            case BOTH_LIMITS_HIT -> rightPower = 0;
            case UPPER_LIMIT_HIT -> rightPower = MathUtils.constrainDouble(rightPower, armPowerMin.get(), 0);
            case LOWER_LIMIT_HIT -> rightPower = MathUtils.constrainDouble(rightPower, 0, armPowerMax.get());
            default -> {}
        }

        // Put power within limit range (if not already)
        leftPower = MathUtils.constrainDouble(leftPower, armPowerMin.get(), armPowerMax.get());
        rightPower = MathUtils.constrainDouble(rightPower, armPowerMin.get(), armPowerMax.get());

        if (contract.isArmReady()) {
            armMotorLeft.set(leftPower);
            armMotorRight.set(rightPower);
        }
    }

    @Override
    public void setPower(Double power) {
        setPowerToLeftAndRightArms(power, power);
    }

    public void extend() {
        setPower(extendPower.get());
        armState = ArmState.EXTENDING;
    }

    public void retract() {
        setPower(retractPower.get());
        armState = ArmState.RETRACTING;
    }

    public void stop() {
        setPower(0.0);
        armState = ArmState.STOPPED;
    }

    public double convertTicksToDistance(double ticks) {
        return ticksToMmRatio.get() * ticks;
    }

    public double convertTicksToShooterAngle(double ticks) {
        return ticks * 1000; // To be modified into ticks to shooter angle formula
    }

    public double convertShooterAngleToTicks(double angle) {
        return 0;
    }


    // Returns an angle for the shooter that can be converted into arm position later if needed
    public double getUsefulArmPosition(UsefulArmPosition usefulArmPosition) {
        double angle;
        switch(usefulArmPosition) {
            // THESE ARE ALL PLACEHOLDER VALUES!!!
            case STARTING_POSITION -> angle = 40;
            case COLLECTING_FROM_GROUND -> angle = 0;
            case FIRING_FROM_SPEAKER_FRONT -> angle = 30;
            case FIRING_IN_AMP -> angle = 80;
            default -> angle = 40;
        }
        return angle;
    }


    public LimitState getLimitState(XCANSparkMax motor) {
        boolean upperHit = false;
        boolean lowerHit = false;

        if (contract.isArmReady()) {
            upperHit = motor.getForwardLimitSwitchPressed(SparkLimitSwitch.Type.kNormallyOpen);
            lowerHit = motor.getReverseLimitSwitchPressed(SparkLimitSwitch.Type.kNormallyOpen);
        }

        if (upperHit && lowerHit) {
            return LimitState.BOTH_LIMITS_HIT;
        } else if (upperHit) {
            return LimitState.UPPER_LIMIT_HIT;
        } else if (lowerHit) {
            return LimitState.LOWER_LIMIT_HIT;
        }
        return LimitState.NOT_AT_LIMIT;
    }


    public void armEncoderTicksUpdate() {
        aKitLog.record("ArmMotorLeftTicks", armMotorLeft.getPosition());
        aKitLog.record("ArmMotorRightTicks", armMotorRight.getPosition());
        aKitLog.record("ArmMotorLeftDistance", convertTicksToDistance(
                armMotorLeft.getPosition() + armMotorLeftRevolutionOffset.get()));
        aKitLog.record("ArmMotorRightDistance", convertTicksToDistance(
                armMotorRight.getPosition() + armMotorRightRevolutionOffset.get()));

        aKitLog.record("ArmMotorToShooterAngle", convertTicksToShooterAngle(
                (armMotorLeft.getPosition() + armMotorRight.getPosition() + armMotorLeftRevolutionOffset.get()
                        + armMotorRightRevolutionOffset.get()) / 2));
    }


    // Update the offset of the arm when it touches either forward/reverse limit switches for the first time.
    public void calibrateArmOffset() {
        LimitState leftArmLimitState = getLimitState(armMotorLeft);
        LimitState rightArmLimitState = getLimitState(armMotorRight);

        if (!hasCalibratedLeft && leftArmLimitState == LimitState.LOWER_LIMIT_HIT) {
            hasCalibratedLeft = true;
            armMotorLeftRevolutionOffset.set(-armMotorLeft.getPosition());
        }

        if (!hasCalibratedRight && rightArmLimitState == LimitState.LOWER_LIMIT_HIT) {
            hasCalibratedRight = true;
            armMotorRightRevolutionOffset.set(-armMotorRight.getPosition());
        }

        aKitLog.record("HasCalibratedLeftArm", hasCalibratedLeft);
        aKitLog.record("HasCalibratedRightArm", hasCalibratedRight);
    }
    @Override
    public Double getCurrentValue() {
        armMotorLeft.getPosition();
        armMotorRight.getPosition();
        //returning 0 for now value will be changed later
        return 0.0;
    }

    @Override
    public Double getTargetValue() {
        return targetAngle;
    }

    @Override
    public void setTargetValue(Double value) {
         targetAngle = value;
    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    public void periodic() {
        if (contract.isArmReady()) {
            armEncoderTicksUpdate();
            calibrateArmOffset();
            armMotorLeft.periodic();
            armMotorRight.periodic();
        }

        aKitLog.record("Target Angle" + targetAngle);
        aKitLog.record("Arm3dState", new Pose3d(
                new Translation3d(0, 0, 0),
                new Rotation3d(0, 0, 0)));
    }

    @Override
    public void refreshDataFrame() {
        if (contract.isArmReady()) {
            armMotorLeft.refreshDataFrame();
            armMotorRight.refreshDataFrame();
        }
    }
}