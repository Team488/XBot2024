package competition.subsystems.arm;

import com.revrobotics.SparkLimitSwitch;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.util.Color8Bit;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XSolenoid;
import xbot.common.controls.sensors.XSparkAbsoluteEncoder;
import xbot.common.math.MathUtils;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ArmSubsystem extends BaseSetpointSubsystem<Double> implements DataFrameRefreshable {

    public XCANSparkMax armMotorLeft;
    public XCANSparkMax armMotorRight;
    public XSolenoid armBrakeSolenoid;
    public XSparkAbsoluteEncoder armAbsoluteEncoder;
    public final ElectricalContract contract;

    public ArmState armState;

    public final DoubleProperty extendPower;
    public final DoubleProperty retractPower;

    private final DoubleProperty armPowerMax;
    private final DoubleProperty armPowerMin;


    public final DoubleProperty ticksToMmRatio; // Millimeters
    private double armMotorLeftRevolutionOffset; // # of revolutions
    private double armMotorRightRevolutionOffset;
    public final DoubleProperty armMotorRevolutionLimit;
    public final DoubleProperty armAbsoluteEncoderOffset;
    public final DoubleProperty armAbsoluteEncoderTicksPerDegree;
    public final DoubleProperty softUpperLimit;
    public final DoubleProperty softLowerLimit;
    public final DoubleProperty softUpperLimitSpeed;
    public final DoubleProperty softLowerLimitSpeed;
    public final DoubleProperty speedLimitForNotCalibrated;
    public final DoubleProperty angleTrim;
    boolean hasCalibratedLeft;
    boolean hasCalibratedRight;
    private final DoubleProperty maximumArmDesyncInMm;

    private double targetExtension;
    private final DoubleProperty armPowerClamp;

    PoseSubsystem pose;
    public final Mechanism2d armActual2d;
    public final MechanismLigament2d armLigament;
    // what angle does the arm make with the pivot when it's at our concept of zero?
    public final double armPivotAngleAtArmAngleZero = 45;

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
        FIRING_IN_AMP,
        SCOOCH_NOTE
    }

    @Inject
    public ArmSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                        XSolenoid.XSolenoidFactory xSolenoidFactory,
                        ElectricalContract contract, PoseSubsystem pose) {
        this.pose = pose;

        this.armBrakeSolenoid = xSolenoidFactory.create(contract.getBrakeSolenoid().channel);
        // THIS IS FOR END OF DAY COMMIT        
        pf.setPrefix(this);
        this.contract = contract;
        setArmBrakeSolenoid(false);
        extendPower = pf.createPersistentProperty("ExtendPower", 0.1);
        retractPower = pf.createPersistentProperty("RetractPower", 0.1);
      
        armPowerMax = pf.createPersistentProperty("ArmPowerMax", 0.5);
        armPowerMin = pf.createPersistentProperty("ArmPowerMin", -0.5);

        // ticksToMmRatio and armMotorRevLimit needs configuration
        ticksToMmRatio = pf.createPersistentProperty("TicksToArmMmRatio", 1000);
        armMotorRevolutionLimit = pf.createPersistentProperty("ArmMotorPositionLimit", 15000);

        angleTrim = pf.createPersistentProperty("AngleTrim", 0);

        armAbsoluteEncoderOffset = pf.createPersistentProperty(
                "ArmAbsoluteEncoderOffset", 0);
        armAbsoluteEncoderTicksPerDegree = pf.createPersistentProperty(
                "ArmAbsoluteEncoderTicksPerDegree", 1);

        softLowerLimit = pf.createPersistentProperty(
                "SoftLowerLimit", armMotorRevolutionLimit.get() * 0.15);
        softUpperLimit = pf.createPersistentProperty(
                "SoftUpperLimit", armMotorRevolutionLimit.get() * 0.85);
        softLowerLimitSpeed = pf.createPersistentProperty("SoftLowerLimitSpeed", -0.05);
        softUpperLimitSpeed = pf.createPersistentProperty("SoftUpperLimitSpeed", 0.05);

        speedLimitForNotCalibrated = pf.createPersistentProperty(
                "SpeedLimitForNotCalibrated", -0.1);

        armPowerClamp = pf.createPersistentProperty("ArmPowerClamp", 0.05);
        maximumArmDesyncInMm = pf.createPersistentProperty("MaximumArmDesyncInMm", 5);

        hasCalibratedLeft = false;
        hasCalibratedRight = false;

        if (contract.isArmReady()) {
            armMotorLeft = sparkMaxFactory.createWithoutProperties(
                    contract.getArmMotorLeft(), this.getPrefix(), "ArmMotorLeft");
            armMotorRight = sparkMaxFactory.createWithoutProperties(
                    contract.getArmMotorRight(), this.getPrefix(), "ArmMotorRight");

            // Get through-bore encoders
            var armWithEncoder = contract.getArmEncoderIsOnLeftMotor() ? armMotorLeft : armMotorRight;
            armAbsoluteEncoder = armWithEncoder.getAbsoluteEncoder(
                    this.getPrefix() + "ArmEncoder",
                    contract.getArmEncoderInverted());

            armMotorLeft.setSmartCurrentLimit(20);
            armMotorRight.setSmartCurrentLimit(20);

            // Enable hardware limits
            armMotorLeft.setForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, true);
            armMotorLeft.setReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, true);
            armMotorRight.setForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, true);
            armMotorRight.setReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, true);
        }

        this.armState = ArmState.STOPPED;

        armActual2d = new Mechanism2d(10, 10, new Color8Bit(255, 255, 255));
        armLigament = new MechanismLigament2d("arm", 5, getArmAngle() - armPivotAngleAtArmAngleZero, 10, new Color8Bit(255, 0, 0)); 
        armActual2d.getRoot("base", 3, 5)
            .append(armLigament)
            .append(new MechanismLigament2d("box-right", 1, 90))
            .append(new MechanismLigament2d("box-top", 2, 90))
            .append(new MechanismLigament2d("box-left", 1, 90));

    }

    public double constrainPowerIfNearLimit(double power, double actualPosition) {
        if (actualPosition >= softUpperLimit.get()) {
            power = MathUtils.constrainDouble(power, armPowerMin.get(), softUpperLimitSpeed.get());
        } else if (actualPosition <= softLowerLimit.get()) {
            power = MathUtils.constrainDouble(power, softLowerLimitSpeed.get(), armPowerMax.get());
        }
        return power;
    }

    public double constrainPowerIfAtLimit(XCANSparkMax motor, double power) {
        switch(getLimitState(motor)) {
            case BOTH_LIMITS_HIT -> power = 0;
            case UPPER_LIMIT_HIT -> power = MathUtils.constrainDouble(power, armPowerMin.get(), 0);
            case LOWER_LIMIT_HIT -> power = MathUtils.constrainDouble(power, 0, armPowerMax.get());
            default -> {}
        }
        return power;
    }

    boolean unsafeMinOrMax = false;

    private double getLeftArmPosition() {
        return armMotorLeft.getPosition() + armMotorLeftRevolutionOffset;
    }

    private double getRightArmPosition() {
        return armMotorRight.getPosition() + armMotorRightRevolutionOffset;
    }
  
    public void setPowerToLeftAndRightArms(double leftPower, double rightPower) {

        // First, if we are calibrated, apply a power factor based on the difference between the two
        // arms to make sure they stay in sync
        if (hasCalibratedLeft && hasCalibratedRight) {
            double distanceLeftAhead = convertTicksToMm(getLeftArmPosition() - getRightArmPosition());

            // If the left arm is ahead, and the left arm wants to go up/forward, reduce its power.
            // If the left arm is ahead, and the left arm wants to go down/backward, make no change to power.
            // If the right arm is ahead, and the right arm wants to go up/forward, reduce its power.
            // If the right arm is ahead, and the right arm wants to go down/backward, make no change to power.

            // If we have to make any changes to power, do so by a factor proportional to the maximum
            // allowed desync in mm. At 50% of the desync, it would restrict power by 50%.

            double potentialReductionFactor = Math.min(1, Math.abs(distanceLeftAhead) / maximumArmDesyncInMm.get());
            aKitLog.record("PotentialReductionFactor", potentialReductionFactor);

            // If left arm is ahead
            if (distanceLeftAhead> 0) {
                // and left arm wants to go more ahead, slow down (or stop)
                if (leftPower > 0) {
                    leftPower *= potentialReductionFactor;
                }
                // and right arm wants to get further behind, slow down (or stop)
                if (rightPower < 0) {
                    rightPower *= potentialReductionFactor;
                }
            }
            // If right arm is ahead
            else if (distanceLeftAhead < 0) {
                // and right arm wants to go more ahead, slow down (or stop)
                if (rightPower > 0) {
                    rightPower *= potentialReductionFactor;
                }
                // and left arm wants to get further behind, slow down (or stop)
                if (leftPower < 0) {
                    leftPower *= potentialReductionFactor;
                }
            }
        }

        // Next, completely flatten the power within a known range.
        // Primarily used for validating the arm behavior with very small power values.
        double clampLimit = Math.abs(armPowerClamp.get());

        leftPower = MathUtils.constrainDouble(leftPower, -clampLimit, clampLimit);
        rightPower = MathUtils.constrainDouble(rightPower, -clampLimit, clampLimit);

        // Next, a sanity check; if we have been grossly misconfigured to where the
        // max/min powers are out of bounds (e.g. a max smaller than min), freeze the arm entirely.
        if (armPowerMax.get() < 0 || armPowerMin.get() > 0 || speedLimitForNotCalibrated.get() > 0) {
            armMotorLeft.set(0);
            armMotorRight.set(0);
            if (!unsafeMinOrMax) {
                log.error("armPowerMax or armPowerMin or speedLimitForNotCalibrated values out of bound!");
                unsafeMinOrMax = true;
            }
            return;
        }
        unsafeMinOrMax = false;

        // If not calibrated, motor can only go down at slow rate since we don't know where we are.
        if (!(hasCalibratedLeft && hasCalibratedRight)) {
            leftPower = MathUtils.constrainDouble(leftPower, speedLimitForNotCalibrated.get(), 0);
            rightPower = MathUtils.constrainDouble(rightPower, speedLimitForNotCalibrated.get(), 0);
        }


        // If calibrated, but near limits, slow the system down a bit so we
        // don't slam into the hard limits.
        if (hasCalibratedLeft && hasCalibratedRight)
        {
            leftPower = constrainPowerIfNearLimit(
                    leftPower,
                    getLeftArmPosition());
            rightPower = constrainPowerIfNearLimit(
                    rightPower,
                    getRightArmPosition());
        }
  
        // If we are actually at our hard limits, stop the motors
        leftPower = constrainPowerIfAtLimit(armMotorLeft, leftPower);
        rightPower = constrainPowerIfAtLimit(armMotorRight, rightPower);

        // Respect overall max/min power limits.
        leftPower = MathUtils.constrainDouble(leftPower, armPowerMin.get(), armPowerMax.get());
        rightPower = MathUtils.constrainDouble(rightPower, armPowerMin.get(), armPowerMax.get());

        if (contract.isArmReady()) {
            armMotorLeft.set(leftPower);
            armMotorRight.set(rightPower);
        }
    }
    //brake solenoid
    public void setArmBrakeSolenoid(boolean on){armBrakeSolenoid.setOn(on);}

    @Override
    public void setPower(Double power) {
        setPowerToLeftAndRightArms(power, power);
    }

    public void dangerousManualSetPowerToBothArms(double power) {
        armMotorLeft.set(power);
        armMotorRight.set(power);
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

    // TO-DO
    public double convertTicksToMm(double ticks) {
        return ticksToMmRatio.get() * ticks;
    }

    public double getArmAngleFromDistance(double distanceFromSpeaker) {
        // Distance: Inches; Angle: Degrees; Distance = Measured Distance - Calibration Offset
        return (0.0019 * Math.pow(distanceFromSpeaker, 2) + (-0.7106 * distanceFromSpeaker) + 82.844) + angleTrim.get();
    }


    // Returns an angle for the shooter that can be converted into arm position later if needed
    public double getUsefulArmPositionAngle(UsefulArmPosition usefulArmPosition) {
        double angle;
        switch(usefulArmPosition) {
            // THESE ARE ALL PLACEHOLDER VALUES!!!
            case STARTING_POSITION -> angle = 40;
            case COLLECTING_FROM_GROUND -> angle = 0;
            case FIRING_FROM_SPEAKER_FRONT -> angle = 30;
            case FIRING_IN_AMP -> angle = 80;
            case SCOOCH_NOTE -> angle = 60; // placeholder value, safe angle to let note through while still low
            default -> angle = 40;
        }
        return angle;
    }


    public LimitState getLimitState(XCANSparkMax motor) {
        boolean upperHit = false;
        boolean lowerHit = false;

        if (contract.isArmReady()) {
            upperHit = motor.getForwardLimitSwitchPressed();
            lowerHit = motor.getReverseLimitSwitchPressed();
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

    public double getArmAbsoluteAngle() {
        return (armAbsoluteEncoder.getPosition() + armAbsoluteEncoderOffset.get()) / armAbsoluteEncoderTicksPerDegree.get();
    }

    public void armEncoderTicksUpdate() {
        aKitLog.record("ArmMotorLeftTicks", armMotorLeft.getPosition());
        aKitLog.record("ArmMotorRightTicks", armMotorRight.getPosition());
        aKitLog.record("ArmMotorLeftMm", convertTicksToMm(getLeftArmPosition()));
        aKitLog.record("ArmMotorRightMm", convertTicksToMm(getRightArmPosition()));
        aKitLog.record("ArmAbsoluteEncoderAngle", getArmAbsoluteAngle());
    }


    // Update the offset of the arm when it touches either forward/reverse limit switches for the first time.
    public void calibrateArmOffset() {
        // For now keeping the concept of left/right having independent calibration,
        // but for simplicity, hitting either limit will calibrate both for now.
        if ((!hasCalibratedLeft || !hasCalibratedRight)
                && (getLimitState(armMotorLeft) == LimitState.LOWER_LIMIT_HIT
                    || getLimitState(armMotorRight) == LimitState.LOWER_LIMIT_HIT))
        {
            hasCalibratedLeft = true;
            hasCalibratedRight = true;
            armMotorLeftRevolutionOffset = -armMotorLeft.getPosition();
            armMotorRightRevolutionOffset = -armMotorRight.getPosition();
        }
    }
   
    /**
     * Get the current angle of the arm in degrees based on the extension distance.
     */
    public double getArmAngle() {
        return getArmAngleForExtension(getCurrentValue());
    }

    public double getArmAngleForExtension(double extension) {
        // TODO: This is just a placeholder, the relationship will actually be nonlinear
        var degreesPerMmExtension = 0.01;
        return extension * degreesPerMmExtension;
    }

    public double getExtensionForArmAngle(double angle) {
        // TODO: this is just a placeholder, the relationship will be nonlinear
        var degreesPerMmExtension = 0.01;

        // unncessarily paranoid avoid divide by 0 check
        if(degreesPerMmExtension == 0) {
            return 0;
        } else {
            return angle / degreesPerMmExtension;
        }
    }

    @Override
    public Double getCurrentValue() {
        return getExtensionDistance();
    }

    public double getExtensionDistance() {
        return convertTicksToMm(armMotorLeft.getPosition() + armMotorLeftRevolutionOffset);
    }

    /** 
     * This is the extension distance the arm is trying to reach via PID 
     */
    @Override
    public Double getTargetValue() {
        return targetExtension;
    }

    /**
     * the current target extension distance the arm is trying to reach via PID
     */
    @Override
    public void setTargetValue(Double targetExtension) {
         this.targetExtension = targetExtension;
    }

    public void setTargetAngle(Double targetAngle) {
        targetExtension = getExtensionForArmAngle(targetAngle);
    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    public double getLeftArmOffset() {
        return armMotorLeftRevolutionOffset;
    }

    public double getRightArmOffset() {
        return armMotorRightRevolutionOffset;
    }

    /**
     * Do not call this from competition code.
     * @param clampPower maximum power under any circumstance
     */
    public void setClampLimit(double clampPower) {
        armPowerClamp.set(Math.abs(clampPower));
    }

    public void periodic() {
        if (contract.isArmReady()) {
            armEncoderTicksUpdate();
            calibrateArmOffset();
            armMotorLeft.periodic();
            armMotorRight.periodic();
        }

        aKitLog.record("HasCalibratedLeftArm", hasCalibratedLeft);
        aKitLog.record("HasCalibratedRightArm", hasCalibratedRight);

        aKitLog.record("Target Extension", targetExtension);
        aKitLog.record("TargetAngle", getArmAngleForExtension(targetExtension));
        aKitLog.record("Arm3dState", new Pose3d(
                new Translation3d(0, 0, 0),
                new Rotation3d(0, 0, 0)));

        var color = isCalibrated() ? new Color8Bit(0, 255, 0) : new Color8Bit(255, 0, 0);
        armLigament.setAngle(getArmAngle() - armPivotAngleAtArmAngleZero);
        armLigament.setColor(color);
        aKitLog.record("Arm2dStateActual", armActual2d);
    }


    @Override
    public void refreshDataFrame() {
        if (contract.isArmReady()) {
            armMotorLeft.refreshDataFrame();
            armMotorRight.refreshDataFrame();
            armAbsoluteEncoder.refreshDataFrame();
        }
    }

    public double getAngleFromRange() {
        return getArmAngleFromDistance(pose.getDistanceFromSpeaker());
    }
}