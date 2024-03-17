package competition.subsystems.arm;

import com.revrobotics.CANSparkBase;
import com.revrobotics.SparkLimitSwitch;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.ScoringLocation;
import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.util.Color8Bit;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCompressor;
import xbot.common.controls.actuators.XDoubleSolenoid;
import xbot.common.controls.actuators.XSolenoid;
import xbot.common.controls.sensors.XSparkAbsoluteEncoder;
import xbot.common.controls.sensors.XTimer;
import xbot.common.math.DoubleInterpolator;
import xbot.common.math.MathUtils;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ArmSubsystem extends BaseSetpointSubsystem<Double> implements DataFrameRefreshable {

    public XCANSparkMax armMotorLeft;
    public XCANSparkMax armMotorRight;
    public XDoubleSolenoid armBrakeSolenoid;
    public XSparkAbsoluteEncoder armAbsoluteEncoder;
    public final ElectricalContract contract;

    public ArmState armState;

    public double extendPower;
    public double retractPower;

    public final DoubleProperty powerMax;
    public final DoubleProperty powerMin;


    public final DoubleProperty extensionMmPerRevolution; // Millimeters
    private double armMotorLeftRevolutionOffset; // # of revolutions
    private double armMotorRightRevolutionOffset;
    public final DoubleProperty upperLegalLimitMm;
    public final DoubleProperty absoluteEncoderOffset;
    public final DoubleProperty absoluteEncoderRevolutionsPerArmDegree;
    public final DoubleProperty upperSlowZoneThresholdMm;
    public final DoubleProperty lowerSlowZoneThresholdMm;
    public final DoubleProperty lowerExtremelySlowZoneThresholdMm;
    public final DoubleProperty upperSlowZonePowerLimit;
    public final DoubleProperty lowerSlowZonePowerLimit;
    public final DoubleProperty lowerExtremelySlowZonePowerLimit;
    public final DoubleProperty powerLimitForNotCalibrated;
    boolean hasCalibratedLeft;
    boolean hasCalibratedRight;
    private final DoubleProperty maximumExtensionDesyncMm;

    public final DoubleProperty maxExtensionForUnderStageMm;
    boolean limitToUnderStage = false;

    private double targetExtension;
    private final DoubleProperty overallPowerClampForTesting;

    final PoseSubsystem pose;
    // what angle does the arm make with the pivot when it's at our concept of zero?
    public final double armPivotAngleAtArmAngleZero = 45;

    private double timeSinceNewTarget = -Double.MAX_VALUE;
    private final DoubleProperty powerRampDurationSec;
    private boolean powerRampingEnabled = true;
    private boolean dynamicBrakingEnabled = false;
    private final XCompressor compressor;
    private int totalLoops = 0;
    private int loopsWhereCompressorRunning = 0;

    //private static double[] experimentalRangesInInches = new double[]{0, 36, 49.5, 63, 80, 111, 136};
    //private static double[] experimentalArmExtensionsInMm = new double[]{0, 0,  20.0, 26, 41, 57,  64};
    // TODO: For now, this was a very ugly and quick way to force the arm low, given that all our scoring is coming
    // from the subwoofer. This will need to be revisited.
    private static double[] experimentalRangesInInches = new double[]{0, 63};
    private static double[] experimentalArmExtensionsInMm = new double[]{0, 0};

    boolean manualHangingModeEngaged = false;
    boolean brakesForceEngaged = false;
    final ArmModelBasedCalculator armModelBasedCalculator;



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
        FIRING_FROM_SUBWOOFER,
        FIRING_FROM_AMP,
        SCOOCH_NOTE,
        HANG_APPROACH,
        PROTECTED_FAR_AMP_SHOT,
        PROTECTED_PODIUM_SHOT,
        COLLECT_DIRECTLY_FROM_SOURCE
    }

    private DoubleInterpolator speakerDistanceToExtensionInterpolator;

    @Inject
    public ArmSubsystem(PropertyFactory pf, XCANSparkMax.XCANSparkMaxFactory sparkMaxFactory,
                        XDoubleSolenoid.XDoubleSolenoidFactory doubleSolenoidFactory,
                        XSolenoid.XSolenoidFactory solenoidFactory,
                        ElectricalContract contract, PoseSubsystem pose,
                        DriveSubsystem drive, XCompressor.XCompressorFactory compressorFactory,
                        ArmModelBasedCalculator armModelBasedCalculator) {

        this.pose = pose;
        this.compressor = compressorFactory.create();
        this.armModelBasedCalculator = armModelBasedCalculator;

        armBrakeSolenoid = doubleSolenoidFactory.create(
                solenoidFactory.create(contract.getBrakeSolenoidForward().channel),
                solenoidFactory.create(contract.getBrakeSolenoidReverse().channel));

        // THIS IS FOR END OF DAY COMMIT        
        pf.setPrefix(this);
        this.contract = contract;
        setBrakeState(false);
        extendPower = 0.1;
        retractPower = -0.1;
      
        powerMax = pf.createPersistentProperty("PowerMax", 0.45);
        powerMin = pf.createPersistentProperty("PowerMin", -0.25);
        upperLegalLimitMm = pf.createPersistentProperty("UpperLegalLimitMm", 238);

        upperSlowZoneThresholdMm = pf.createPersistentProperty(
                "UpperSlowZoneThresholdMm", upperLegalLimitMm.get() * 0.85);
        lowerSlowZoneThresholdMm = pf.createPersistentProperty(
                "LowerSlowZoneThresholdMm", 25.0);
        lowerExtremelySlowZoneThresholdMm = pf.createPersistentProperty(
                "LowerExtremelySlowZoneThresholdMm", upperLegalLimitMm.get() * 0.05);

        maxExtensionForUnderStageMm = pf.createPersistentProperty("MaxExtensionForUnderStageMm", 52.0);

        upperSlowZonePowerLimit = pf.createPersistentProperty("UpperSlowZonePowerLimit", 0.10);
        lowerSlowZonePowerLimit = pf.createPersistentProperty("LowerSlowZonePowerLimit", -0.05);
        lowerExtremelySlowZonePowerLimit = pf.createPersistentProperty("LowerExtremelySlowZonePowerLimit", -0.03);

        powerLimitForNotCalibrated = pf.createPersistentProperty(
                "PowerLimitForNotCalibrated", -0.03);

        overallPowerClampForTesting = pf.createPersistentProperty("overallTestingPowerClamp", 0.45);
        maximumExtensionDesyncMm = pf.createPersistentProperty("MaximumExtensionDesyncMm", 6.0);


        pf.setDefaultLevel(Property.PropertyLevel.Debug);
        powerRampDurationSec = pf.createPersistentProperty("PowerRampDurationSec", 0.5);
        absoluteEncoderOffset = pf.createPersistentProperty(
                "AbsoluteEncoderOffset", 0);
        absoluteEncoderRevolutionsPerArmDegree = pf.createPersistentProperty(
                "AbsoluteEncoderRevolutionPerArmDegree", 1);
        extensionMmPerRevolution = pf.createPersistentProperty("ExtensionMmPerRevolution", 5.715352326);

        hasCalibratedLeft = false;
        hasCalibratedRight = false;

        if (contract.isArmReady()) {
            armMotorLeft = sparkMaxFactory.createWithoutProperties(
                    contract.getArmMotorLeft(), this.getPrefix(), "ArmMotorLeft");
            armMotorRight = sparkMaxFactory.createWithoutProperties(
                    contract.getArmMotorRight(), this.getPrefix(), "ArmMotorRight");

            armMotorLeft.enableVoltageCompensation(12);
            armMotorRight.enableVoltageCompensation(12);

            // Get through-bore encoder -
            // this is plugged in to the front left drive motor controller
            if (contract.isDriveReady()) {
                armAbsoluteEncoder = drive.getFrontLeftSwerveModuleSubsystem()
                        .getDriveSubsystem().getSparkMax().getAbsoluteEncoder(
                                this.getPrefix() + "ArmEncoder",
                                contract.getArmEncoderInverted());
            }

            armMotorLeft.setSmartCurrentLimit(60);
            armMotorRight.setSmartCurrentLimit(60);

            // Enable hardware limits
            armMotorLeft.setForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, true);
            armMotorLeft.setReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, false);
            armMotorRight.setForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, true);
            armMotorRight.setReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen, false);

            armMotorLeft.setIdleMode(CANSparkBase.IdleMode.kCoast);
            armMotorRight.setIdleMode(CANSparkBase.IdleMode.kCoast);
        }

        this.armState = ArmState.STOPPED;

        double[] rangesInMeters = new double[experimentalRangesInInches.length];
        //PoseSubsystem.INCHES_IN_A_METER
        // Convert the rangesInInches array to meters
        for (int i = 0; i < experimentalRangesInInches.length; i++) {
            rangesInMeters[i] = experimentalRangesInInches[i] / PoseSubsystem.INCHES_IN_A_METER;
        }

        speakerDistanceToExtensionInterpolator =
                new DoubleInterpolator(
                        rangesInMeters,
                        experimentalArmExtensionsInMm);
    }

    public double constrainPowerIfNearLimit(double power, double actualPosition) {
        if (actualPosition >= upperLegalLimitMm.get()) {
            power = MathUtils.constrainDouble(power, powerMin.get(), 0);
        } else if (actualPosition >= upperSlowZoneThresholdMm.get()) {
            power = MathUtils.constrainDouble(power, powerMin.get(), upperSlowZonePowerLimit.get());
        } else if (actualPosition <= lowerSlowZoneThresholdMm.get()
                && actualPosition > lowerExtremelySlowZoneThresholdMm.get()
                && !manualHangingModeEngaged) {
            power = MathUtils.constrainDouble(power, lowerSlowZonePowerLimit.get(), powerMax.get());
        } else if (actualPosition <= lowerExtremelySlowZoneThresholdMm.get()) {
            power = MathUtils.constrainDouble(power, lowerExtremelySlowZonePowerLimit.get(), powerMax.get());
        }
        return power;
    }

    public double constrainPowerIfAtLimit(XCANSparkMax motor, double power) {
        switch(getLimitState(motor)) {
            case BOTH_LIMITS_HIT -> power = 0;
            case UPPER_LIMIT_HIT -> power = MathUtils.constrainDouble(power, powerMin.get(), 0);
            case LOWER_LIMIT_HIT -> power = MathUtils.constrainDouble(power, 0, powerMax.get());
            default -> {}
        }
        return power;
    }

    boolean unsafeMinOrMax = false;

    private double getLeftArmPositionInRevolutions() {
        return armMotorLeft.getPosition() + armMotorLeftRevolutionOffset;
    }

    private double getRightArmPositionInRevolutions() {
        return armMotorRight.getPosition() + armMotorRightRevolutionOffset;
    }
  
    public void setPowerToLeftAndRightArms(double leftPower, double rightPower) {

        // First, if we are calibrated, apply a power factor based on the difference between the two
        // arms to make sure they stay in sync
        if (hasCalibratedLeft && hasCalibratedRight) {
            double distanceLeftAhead = convertRevolutionsToExtensionMm(getLeftArmPositionInRevolutions() - getRightArmPositionInRevolutions());
            aKitLog.record("DistanceLeftAhead", distanceLeftAhead);
            // If the left arm is ahead, and the left arm wants to go up/forward, reduce its power.
            // If the left arm is ahead, and the left arm wants to go down/backward, make no change to power.
            // If the right arm is ahead, and the right arm wants to go up/forward, reduce its power.
            // If the right arm is ahead, and the right arm wants to go down/backward, make no change to power.

            // If we have to make any changes to power, do so by a factor proportional to the maximum
            // allowed desync in mm. At 50% of the desync, it would restrict power by 50%.

            double potentialReductionFactor = Math.max(0, 1 - Math.abs(distanceLeftAhead) / maximumExtensionDesyncMm.get());
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
        double clampLimit = Math.abs(overallPowerClampForTesting.get());

        leftPower = MathUtils.constrainDouble(leftPower, -clampLimit, clampLimit);
        rightPower = MathUtils.constrainDouble(rightPower, -clampLimit, clampLimit);

        // Next, a sanity check; if we have been grossly misconfigured to where the
        // max/min powers are out of bounds (e.g. a max smaller than min), freeze the arm entirely.
        if (powerMax.get() < 0 || powerMin.get() > 0 || powerLimitForNotCalibrated.get() > 0) {
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
            leftPower = MathUtils.constrainDouble(leftPower, powerLimitForNotCalibrated.get(), 0);
            rightPower = MathUtils.constrainDouble(rightPower, powerLimitForNotCalibrated.get(), 0);
        }

        // If calibrated, but near limits, slow the system down a bit so we
        // don't slam into the hard limits.
        if (hasCalibratedLeft && hasCalibratedRight)
        {
            leftPower = constrainPowerIfNearLimit(
                    leftPower,
                    convertRevolutionsToExtensionMm(getLeftArmPositionInRevolutions()));
            rightPower = constrainPowerIfNearLimit(
                    rightPower,
                    convertRevolutionsToExtensionMm(getRightArmPositionInRevolutions()));
        }

        // If we are actually at our hard limits, stop the motors
        leftPower = constrainPowerIfAtLimit(armMotorLeft, leftPower);
        rightPower = constrainPowerIfAtLimit(armMotorRight, rightPower);

        // Respect overall max/min power limits.
        if (!manualHangingModeEngaged) {
            leftPower = MathUtils.constrainDouble(leftPower, powerMin.get(), powerMax.get());
            rightPower = MathUtils.constrainDouble(rightPower, powerMin.get(), powerMax.get());
        } else {
            // we need more power to hang
            leftPower = MathUtils.constrainDouble(leftPower, -powerMax.get(), powerMax.get());
            rightPower = MathUtils.constrainDouble(rightPower, -powerMax.get(), powerMax.get());
        }

        // Try to ramp power - a smoother start will definitely help reduce high current shock loads,
        // and may reduce instability if both sides can get up to "cruise speed" together
        if (powerRampingEnabled && powerRampDurationSec.get() > 0) {
            double timeSince = XTimer.getFPGATimestamp() - timeSinceNewTarget;
            if (timeSince < powerRampDurationSec.get()) {
                double rampFactor = timeSince / powerRampDurationSec.get();
                leftPower *= rampFactor;
                rightPower *= rampFactor;
            }
        }

        // Engage brake if no power commanded
        if (leftPower == 0 && rightPower == 0) {
            setBrakeState(true);
        } else {
            // Disengage brake if any power commanded.
            setBrakeState(false);
        }

        // finally, if the brake is engaged, just stop the motors.
        if (getBrakeEngaged()) {
            leftPower = 0;
            rightPower = 0;
        }

        if (contract.isArmReady()) {
            armMotorLeft.set(leftPower);
            armMotorRight.set(rightPower);
        }
    }

    boolean brakeEngaged = false;
    //brake solenoid
    public void setBrakeState(boolean enabled) {
        if (brakesForceEngaged) {
            enabled = true;
        }

        brakeEngaged = enabled;
        if (enabled) {
            armBrakeSolenoid.setForward();
        } else {
            armBrakeSolenoid.setReverse();
        }
    }

    public boolean getBrakeEngaged() {
        return brakeEngaged;
    }

    /**
     * Forces the brakes on, even if other callers try to free them.
     * @param brakesForceEngaged if true, brakes will stay permanently engaged until this is called again with false
     */
    public void setForceBrakesEngaged(boolean brakesForceEngaged) {
        this.brakesForceEngaged = brakesForceEngaged;
        if (brakesForceEngaged) {
            setBrakeState(true);
        }
    }

    public boolean getForceBrakesEngaged() {
        return brakesForceEngaged;
    }

    double previousPower;

    @Override
    public void setPower(Double power) {

        if (previousPower == 0 && power != 0) {
            initializeRampingPowerTarget();
        }

        aKitLog.record("RequestedArmPower", power);
        setPowerToLeftAndRightArms(power, power);
        previousPower = power;
    }

    public void dangerousManualSetPowerToBothArms(double power) {
        setForceBrakesEngaged(false);
        setBrakeState(false);
        if (contract.isArmReady()) {
            armMotorLeft.set(power);
            armMotorRight.set(power);
        }
    }

    public void extend() {
        setPower(extendPower);
        armState = ArmState.EXTENDING;
    }

    public void retract() {
        setPower(retractPower);
        armState = ArmState.RETRACTING;
    }

    public void stop() {
        setPower(0.0);
        armState = ArmState.STOPPED;
    }

    // TO-DO
    public double convertRevolutionsToExtensionMm(double revolutions) {
        return extensionMmPerRevolution.get() * revolutions;
    }

    public double getModeledExtensionForGivenSpeakerDistance(double distanceFromSpeaker) {
        return armModelBasedCalculator.getExtensionForSpeakerDistance(distanceFromSpeaker);
    }

    public double getUsefulArmPositionExtensionInMm(UsefulArmPosition usefulArmPosition) {
        double extension = 0;
        switch (usefulArmPosition) {
            case STARTING_POSITION:
            case COLLECTING_FROM_GROUND:
            case FIRING_FROM_SUBWOOFER:
                extension = 0;
                break;
            case FIRING_FROM_AMP:
                extension = upperLegalLimitMm.get();
                break;
            case SCOOCH_NOTE:
                extension = 30;
                break;
            case PROTECTED_FAR_AMP_SHOT:
                extension = 71.1;
                break;
            case PROTECTED_PODIUM_SHOT:
                extension = 58.81;
                break;
            case COLLECT_DIRECTLY_FROM_SOURCE:
                extension = 180;
                break;
            case HANG_APPROACH:
                extension = 100;
                break;
            default:
                return 0;
        }
        return extension;
    }

    public double getUsefulArmPositionExtensionInMm(PointOfInterest pointOfInterest) {
        double extension = 0;
        switch (pointOfInterest) {
            case SubwooferTopScoringLocation:
            case SubwooferMiddleScoringLocation:
            case SubwooferBottomScoringLocation:
                extension = 0;
                break;
            case PodiumScoringLocation:
                extension = 58.81;
                break;
            case AmpFarScoringLocation:
                extension = 71.1;
                break;
            case SpikeMiddle:
                extension = 57;
                break;
            case BottomSpikeCloserToSpeakerScoringLocation:
            case TopSpikeCloserToSpeakerScoringLocation:
                extension = 57; // TODO - what are the real numbers?
                break;
            case OneRobotAwayFromCenterSubwooferScoringLocation:
                extension = 44.8;
                break;
            default:
                return 0;
        }
        return extension;
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

    public void recordArmEncoderValues() {
        aKitLog.record("LeftExtensionMm", convertRevolutionsToExtensionMm(getLeftArmPositionInRevolutions()));
        aKitLog.record("RightExtensionMm", convertRevolutionsToExtensionMm(getRightArmPositionInRevolutions()));
    }


    // Update the offset of the arm when it touches reverse limit switches for the first time.
    // Both arms calibrate independently
    public void calibrateArmOffset() {
        if (!hasCalibratedLeft && getLimitState(armMotorLeft) == LimitState.LOWER_LIMIT_HIT) {
            hasCalibratedLeft = true;
            armMotorLeftRevolutionOffset = -armMotorLeft.getPosition();
        }

        if (!hasCalibratedRight && getLimitState(armMotorRight) == LimitState.LOWER_LIMIT_HIT) {
            hasCalibratedRight = true;
            armMotorRightRevolutionOffset = -armMotorRight.getPosition();
        }
    }

    @Override
    public Double getCurrentValue() {
        if (contract.isArmReady()) {
            return getExtensionDistance();
        }
        return 0.0;
    }

    public double getExtensionDistance() {
        return convertRevolutionsToExtensionMm(armMotorLeft.getPosition() + armMotorLeftRevolutionOffset);
    }

    /** 
     * This is the extension distance the arm is trying to reach via PID 
     */
    @Override
    public Double getTargetValue() {
        return targetExtension;
    }

    public void setTargetValue(UsefulArmPosition usefulArmPosition) {
        setTargetValue(getUsefulArmPositionExtensionInMm(usefulArmPosition));
    }

    /**
     * the current target extension distance the arm is trying to reach via PID
     */
    @Override
    public void setTargetValue(Double targetExtension) {
         this.targetExtension = targetExtension;
    }

    public Double getSafeTargetValue() {
        if(getLimitToUnderStage()) {
            return Math.min(getTargetValue(), maxExtensionForUnderStageMm.get());
        }
        return getTargetValue();
    }

    @Override
    public boolean isCalibrated() {
        return hasCalibratedLeft && hasCalibratedRight;
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
        overallPowerClampForTesting.set(Math.abs(clampPower));
    }

    public void initializeRampingPowerTarget() {
        timeSinceNewTarget = XTimer.getFPGATimestamp();
    }

    public void setRampingPowerEnabled(boolean enabled) {
        powerRampingEnabled = enabled;
    }

    public void markArmsAsCalibratedAgainstLowerPhyscalLimit() {
        hasCalibratedLeft = true;
        armMotorLeftRevolutionOffset = -armMotorLeft.getPosition();
        hasCalibratedRight = true;
        armMotorRightRevolutionOffset = -armMotorRight.getPosition();
    }

    public double getRecommendedExtension(double distanceFromSpeaker) {
        return speakerDistanceToExtensionInterpolator.getInterpolatedOutputVariable(distanceFromSpeaker);
    }

    public double getRecommendedExtensionForSpeaker() {
        return getRecommendedExtension(pose.getDistanceFromSpeaker());
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Double target1, Double target2) {
        return BaseSetpointSubsystem.areTwoDoublesEquivalent(target1, target2, 1);
    }

    /**
     * Returns our maximum scoring range. Is useful if the arm is "at target"
     * but we know there's no way it will actually score.
     * @return the maximum scoring range in meters
     */
    public double getMaximumRangeForAnyShotMeters() {
        return experimentalRangesInInches[experimentalRangesInInches.length - 1] / PoseSubsystem.INCHES_IN_A_METER;
    }

    public boolean getManualHangingMode() {
        return manualHangingModeEngaged;
    }

    public boolean couldPlausiblyBeHanging() {
        return getExtensionDistance() < 15;
    }

    public void setManualHangingMode(boolean enabled) {
        manualHangingModeEngaged = enabled;
    }

    public void periodic() {
        if (contract.isArmReady()) {
            recordArmEncoderValues();
            calibrateArmOffset();
            armMotorLeft.periodic();
            armMotorRight.periodic();
        }

        aKitLog.record("HasCalibratedLeftArm", hasCalibratedLeft);
        aKitLog.record("HasCalibratedRightArm", hasCalibratedRight);
        aKitLog.record("BrakeEngaged", getBrakeEngaged());
        aKitLog.record("ForceBrakesEngaged", getForceBrakesEngaged());
        aKitLog.record("Target Extension", targetExtension);
        aKitLog.record("LimitToUnderStage", getLimitToUnderStage());
        aKitLog.record("Safe Target Extension", getSafeTargetValue());
        aKitLog.record("Arm3dState", new Pose3d(
                new Translation3d(0, 0, 0),
                new Rotation3d(0, 0, 0)));

        if (DriverStation.isEnabled()) {
            totalLoops++;
            if (!compressor.isAtTargetPressure()) {
                loopsWhereCompressorRunning++;
            }
        }
        if (totalLoops > 0) {
            aKitLog.record("CompressorRunningPercentage", (loopsWhereCompressorRunning * 100) / totalLoops);
        }
    }


    @Override
    public void refreshDataFrame() {
        if (contract.isArmReady()) {
            armMotorLeft.refreshDataFrame();
            armMotorRight.refreshDataFrame();
            armAbsoluteEncoder.refreshDataFrame();
        }
    }

    public void setLimitToUnderStage(boolean limitToUnderStage) {
        this.limitToUnderStage = limitToUnderStage;
    }

    public boolean getLimitToUnderStage() {
        return limitToUnderStage;
    }
}