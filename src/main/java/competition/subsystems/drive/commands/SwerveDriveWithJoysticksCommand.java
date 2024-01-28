package competition.subsystems.drive.commands;

import javax.inject.Inject;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.logic.HumanVsMachineDecider.HumanVsMachineDeciderFactory;
import xbot.common.logic.HumanVsMachineDecider.HumanVsMachineMode;
import xbot.common.logic.Latch;
import xbot.common.logic.Latch.EdgeType;
import xbot.common.math.ContiguousDouble;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.drive.control_logic.HeadingModule.HeadingModuleFactory;

/**
 * The main swerve drive command that links up the human input (from gamepad
 * joysticks) to the drive subsystem.
 */
public class SwerveDriveWithJoysticksCommand extends BaseCommand {

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final OperatorInterface oi;
    final DoubleProperty input_exponent;
    final DoubleProperty drivePowerFactor;
    final DoubleProperty turnPowerFactor;
    final BooleanProperty absoluteOrientationMode;
    final HeadingModule headingModule;
    final Latch absoluteOrientationLatch;
    final DoubleProperty minimumMagnitudeForAbsoluteHeading;
    final DoubleProperty triggerOnlyPowerScaling;
    final DoubleProperty triggerOnlyExponent;
    final HumanVsMachineDecider decider;
    DriverStation.Alliance alliance;

    @Inject
    public SwerveDriveWithJoysticksCommand(
            DriveSubsystem drive, PoseSubsystem pose, OperatorInterface oi,
            PropertyFactory pf, HumanVsMachineDeciderFactory hvmFactory, HeadingModuleFactory headingModuleFactory) {
        this.drive = drive;
        this.oi = oi;
        this.pose = pose;
        pf.setPrefix(this);
        this.input_exponent = pf.createPersistentProperty("Input Exponent", 2);
        this.drivePowerFactor = pf.createPersistentProperty("Power Factor", 0.75);
        this.turnPowerFactor = pf.createPersistentProperty("Turn Power Factor", 0.75);
        this.absoluteOrientationMode = pf.createPersistentProperty("Absolute Orientation Mode", true);
        this.minimumMagnitudeForAbsoluteHeading = pf.createPersistentProperty("Min Magnitude For Absolute Heading", 0.75);
        this.decider = hvmFactory.create(this.getPrefix());
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.triggerOnlyPowerScaling = pf.createPersistentProperty("TriggerOnlyPowerScaling", 0.75);
        this.triggerOnlyExponent = pf.createPersistentProperty("TriggerOnlyExponent", 2.0);

        // Set up a latch to trigger whenever we change the rotational mode. In either case,
        // there's some PIDs that will need to be reset, or goals that need updating.
        absoluteOrientationLatch = new Latch(absoluteOrientationMode.get(), EdgeType.Both, edge -> {
            if(edge == EdgeType.RisingEdge) {
                resetBeforeStartingAbsoluteOrientation();
            }
            else if(edge == EdgeType.FallingEdge) {
                resetBeforeStartingRelativeOrientation();
            }
        });

        this.addRequirements(drive);
    }

    public void setAbsoluteHeadingMode(boolean absoluteHeadingEnabled) {
        absoluteOrientationMode.set(absoluteHeadingEnabled);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        decider.reset();
        resetBeforeStartingAbsoluteOrientation();
        resetBeforeStartingRelativeOrientation();
    }

    private void resetBeforeStartingAbsoluteOrientation() {
        // Set our desired heading to the current heading to avoid a surprising
        // change of heading during reset.
        drive.setDesiredHeading(pose.getCurrentHeading().getDegrees());
    }

    private void resetBeforeStartingRelativeOrientation() {
        headingModule.reset();
    }

    @Override
    public void execute() {
        // Feed the latch with our mode state, so it can reset PIDs or goals as appropriate.
        // This will automatically reset the relevant PIDs - you can see what is exactly is registered
        // by looking at the latch code in the constructor.
        absoluteOrientationLatch.setValue(absoluteOrientationMode.get());

        // --------------------------------------------------
        // Translation
        // --------------------------------------------------

        // Get the current translation vector from the gamepad.
        XYPair scaledTranslationIntent = getTranslationIntentFromDriver();

        // --------------------------------------------------
        // Rotation
        // --------------------------------------------------

        // Grab all human sources of rotation intent
        double humanRotateIntentFromTriggers = getRotationIntentFromDriverTriggers();
        double humanRotateIntentFromStick = getRotationIntentFromDriverJoystick();

        // Fuse them together while keeping them in the -1 to 1 range. This is to help avoid doing some kind of
        // conflicting move like trying to rotate in two directions at once.
        double fusedHumanRotateIntent = MathUtils.constrainDoubleToRobotScale(
                humanRotateIntentFromStick + humanRotateIntentFromTriggers);

        double rotateIntent = 0;
        if (absoluteOrientationMode.get()) {
            rotateIntent = getSuggestedRotateIntentForAbsoluteStickControl(humanRotateIntentFromTriggers);
        } else {
            // If we are in the typical "rotate using joystick to turn" mode, use the Heading Assist module to get the suggested power.
            rotateIntent = scaleHumanRotationInput(fusedHumanRotateIntent);
        }

        // --------------------------------------------------
        // Safety scaling and sending to Drive Subsystem
        // --------------------------------------------------

        // Apply various form of scaling (e.g. precision modes) to hte intents.
        scaledTranslationIntent = scaleTranslationIntent(scaledTranslationIntent);
        if (!drive.isUnlockFullDrivePowerActive()) {
            rotateIntent *= turnPowerFactor.get();
        }

        // Log these just before sending them to the drive.
        aKitLog.record("TranslationIntent", scaledTranslationIntent);
        aKitLog.record("SuggestedRotateIntent", rotateIntent);

        // Finally, send the intents as either robot-relative or field-relative.
        if (drive.isRobotOrientedDriveActive()) {
            drive.move(scaledTranslationIntent, rotateIntent);
        } else {
            drive.fieldOrientedDrive(scaledTranslationIntent, rotateIntent, pose.getCurrentHeading().getDegrees(), new XYPair(0,0));
        }
    }

    private double getSuggestedRotateIntentForAbsoluteStickControl(double humanRotatePowerFromTriggers) {
        double suggestedRotatePower;
        // If we are using absolute orientation, we first need get the desired heading from the right joystick.
        // We need to only do this if the joystick has been moved past the minimumMagnitudeForAbsoluteHeading.
        // In the future, we might be able to replace the joystick with a dial or other device that can more easily
        // hold a heading.

        // One key note - we need to invert the right X, since it is already inverted once to make "typical" rotation commands
        // line up with our conventions. (Usually, a right turn is done by moving the joystick right. However, turning to the right
        // is a "negative" rotation, so the X axis is usually inverted to take that into account).
        // By doing this inversion, the vector will better map onto a typical cartesian coordinate system.
        XYPair headingVector = new XYPair(-oi.driverGamepad.getRightStickX(), oi.driverGamepad.getRightStickY());
        // pose.rotateVectorBasedOnAlliance(headingVector);

        // The next step is to rotate the vector. The FRC frame assumes "forward" is 0 degrees, but the typical cartesian setup
        // of a joystick would have "forward" as 90 degrees.
        headingVector = headingVector.rotate(-90);

        double desiredHeading = 0;

        if (headingVector.getMagnitude() > minimumMagnitudeForAbsoluteHeading.get() || drive.isQuickAlignActive()) {
            // If the magnitude is greater than the minimum magnitude, we can use the joystick to set the heading.

            double headingToEvaluateForQuadrant = 0;
            if (drive.isQuickAlignActive()) {
                headingToEvaluateForQuadrant = pose.getCurrentHeading().getDegrees();
            } else {
                headingToEvaluateForQuadrant = headingVector.getAngle();
            }

            double reboundCurrentHeading = ContiguousDouble.reboundValue(headingToEvaluateForQuadrant, -45, 315)+45;
            // Now, we can use the modulus operator to get the quadrant.
            int quadrant = (int) (reboundCurrentHeading / 90);
            desiredHeading = quadrant * 90;

            if (pose.getHeadingResetRecently()) {
                drive.setDesiredHeading(pose.getCurrentHeading().getDegrees());
            } else {
                drive.setDesiredHeading(desiredHeading);
            }
            suggestedRotatePower = headingModule.calculateHeadingPower(desiredHeading);
            decider.reset();
        } else {
            // If the joystick isn't deflected enough, we use the last known heading or human input.
            HumanVsMachineMode recommendedMode = decider.getRecommendedMode(humanRotatePowerFromTriggers);

            if (pose.getHeadingResetRecently()) {
                drive.setDesiredHeading(pose.getCurrentHeading().getDegrees());
            }

            switch (recommendedMode) {
                case Coast:
                    suggestedRotatePower = 0;
                    break;
                case HumanControl:
                    suggestedRotatePower = scaleHumanRotationInput(humanRotatePowerFromTriggers);
                    break;
                case InitializeMachineControl:
                    drive.setDesiredHeading(pose.getCurrentHeading().getDegrees());
                    suggestedRotatePower = 0;
                    break;
                case MachineControl:
                    if (drive.isManualBalanceModeActive()) {
                        suggestedRotatePower = 0;
                    } else {
                        desiredHeading = drive.getDesiredHeading();
                        suggestedRotatePower = headingModule.calculateHeadingPower(desiredHeading);
                    }
                    break;
                default:
                    suggestedRotatePower = 0;
                    break;
            }
        }
        return suggestedRotatePower;
    }

    private XYPair scaleTranslationIntent(XYPair translationIntent) {
        // It's not sensible to magnitudes greater than the unit vector. This isn't Quake - you can't go extra fast by moving diagonally!
        if (translationIntent.getMagnitude() > 1) {
            translationIntent.scale(1/ translationIntent.getMagnitude());
        }

        // Scale the power down if we are in one or more precision modes
        // Rotation is scaled when deciding on human vs machine inputs

        // First, scale down translation, unless we are in full power mode.
        if (!drive.isUnlockFullDrivePowerActive()) {

            // If we are in a precision mode (used for lining up on delicate targets), slow down dramatically
            if (drive.isPrecisionTranslationActive()) {
                translationIntent = translationIntent.scale(0.50);
            } else if (drive.isExtremePrecisionTranslationActive()) {
                translationIntent = translationIntent.scale(0.15);
            }

            // In all cases, scale the global intent down. This is often used when novices are controlling the robot.
            // For competition, it should be at or close to 1.
            translationIntent = translationIntent.scale(drivePowerFactor.get());

            // Similarly, slow down rotation. This is commonly set to smaller values to keep rotation in
            // a human-controllable region; swerve drives can rotate the chassis extremely fast.

        }
        return translationIntent;
    }

    /**
     * Gets the translation intent from the driver's gamepad. It will also
     * scale the magnitude of the vector according to the following rules:
     * 1. If the magnitude is less than the deadband, set it to zero
     * 2. If the magnitude is greater than the deadband, exponentiate it and retain the sign.
     *      as an example, if the exponent is 2, then a magnitude of 0.5 will become 0.25. In general,
     *      higher exponentiation makes the robot respond slower to small inputs, and makes a sharp(er) jump as you
     *      approach full power.
     *
     * @return XYPair representing the deadbanded & scaled translation intent
     */
    private XYPair getTranslationIntentFromDriver() {
        XYPair rawTranslationVector = new XYPair(oi.driverGamepad.getLeftStickX(), oi.driverGamepad.getLeftStickY());
        // preserve the angle
        double rawAngle = rawTranslationVector.getAngle();
        // scale the magnitude of the vector according to the following rules:
        // 1. If the magnitude is less than the deadband, set it to zero
        // 2. If the magnitude is greater than the deadband, exponentiate it and retain the sign.
        //    as an example, if the exponent is 2, then a magnitude of 0.5 will become 0.25. In general,
        //    higher exponentiation makes the robot respond slower to small inputs, and makes a sharp(er) jump as you
        //    approach full power.
        double updatedMagnitude = MathUtils.deadband(
                rawTranslationVector.getMagnitude(),
                oi.getDriverGamepadTypicalDeadband(),
                (a) -> MathUtils.exponentAndRetainSign(a, (int) input_exponent.get()));

        // create new vector with the scaled magnitude and angle
        XYPair translationIntent = XYPair.fromPolar(rawAngle-90, updatedMagnitude);
        return translationIntent;
    }

    private double getRotationIntentFromDriverTriggers() {
        double humanRotatePowerFromLeftTrigger = MathUtils.deadband(
                oi.driverGamepad.getLeftTrigger(),
                0.005,
                (a) -> MathUtils.exponentAndRetainSign(a, (int) triggerOnlyExponent.get()));

        double humanRotatePowerFromRightTrigger = MathUtils.deadband(
                oi.driverGamepad.getRightTrigger(),
                0.005,
                (a) -> MathUtils.exponentAndRetainSign(a, (int) triggerOnlyExponent.get()));

        double humanRotatePowerFromTriggers = humanRotatePowerFromLeftTrigger - humanRotatePowerFromRightTrigger;

        // In the past the driver has wanted additional power damping when rotating, separate from the general
        // scaling shared by the translation and rotation inputs. Regular behavior can be restored by enabling the
        // "unlock full drive power" mode.
        if (!drive.isUnlockFullDrivePowerActive()) {
            humanRotatePowerFromTriggers *= triggerOnlyPowerScaling.get();
        }

        return humanRotatePowerFromTriggers;
    }

    private double getRotationIntentFromDriverJoystick() {
        return MathUtils.deadband(
                oi.driverGamepad.getRightStickX(),
                oi.getDriverGamepadTypicalDeadband(),
                (a) -> MathUtils.exponentAndRetainSign(a, (int) input_exponent.get()));

    }

    private double scaleHumanRotationInput(double humanInputPower) {
        if (drive.isPrecisionRotationActive() || drive.isRobotOrientedDriveActive()) {
            return humanInputPower * 0.25;
        }
        return humanInputPower;
    }
}