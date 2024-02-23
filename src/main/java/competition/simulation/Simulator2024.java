package competition.simulation;

import com.revrobotics.CANSparkBase;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import org.littletonrobotics.junction.Logger;
import xbot.common.controls.actuators.mock_adapters.MockCANSparkMax;
import xbot.common.controls.sensors.XTimer;
import xbot.common.math.MovingAverageForDouble;
import xbot.common.math.MovingAverageForTranslation2d;

import javax.inject.Inject;

public class Simulator2024 {

    MovingAverageForTranslation2d translationAverageCalculator =
            new MovingAverageForTranslation2d(15);
    MovingAverageForDouble rotationAverageCalculator =
            new MovingAverageForDouble(15);
    MovingAverageForDouble leftArmPositionCalculator =
            new MovingAverageForDouble(15);
    MovingAverageForDouble rightArmPositionCalculator =
            new MovingAverageForDouble(15);
    MovingAverageForDouble shooterVelocityCalculator =
            new MovingAverageForDouble(50);

    private final PoseSubsystem pose;
    private final DriveSubsystem drive;
    private final ArmSubsystem arm;
    private final ShooterWheelSubsystem shooter;
    private final CollectorSubsystem collector;

    @Inject
    public Simulator2024(PoseSubsystem pose, DriveSubsystem drive,
                         ArmSubsystem arm, ShooterWheelSubsystem shooter,
                         CollectorSubsystem collector) {
        this.pose = pose;
        this.drive = drive;
        this.arm = arm;
        this.shooter = shooter;
        this.collector = collector;
    }



    int loop = 0;

    public void update() {
        double robotTopSpeedInMetersPerSecond = 3.0;
        double robotLoopPeriod = 0.02;
        double poseAdjustmentFactorForSimulation = robotTopSpeedInMetersPerSecond * robotLoopPeriod;

        double robotTopAngularSpeedInDegreesPerSecond = 360;
        double headingAdjustmentFactorForSimulation = robotTopAngularSpeedInDegreesPerSecond * robotLoopPeriod;


        var currentPose = pose.getCurrentPose2d();

        // Extremely simple physics simulation. We want to give the robot some very basic translational and rotational
        // inertia. We can take the moving average of the last second or so of robot commands and apply that to the
        // robot's pose. This is a very simple way to simulate the robot's movement without having to do any real physics.

        translationAverageCalculator.add(drive.lastRawCommandedDirection);
        var currentAverage = translationAverageCalculator.getAverage();

        rotationAverageCalculator.add(drive.lastRawCommandedRotation);
        var currentRotationAverage = rotationAverageCalculator.getAverage();

        var updatedPose = new Pose2d(
                new Translation2d(
                        currentPose.getTranslation().getX() + currentAverage.getX() * poseAdjustmentFactorForSimulation,
                        currentPose.getTranslation().getY() + currentAverage.getY() * poseAdjustmentFactorForSimulation),
                currentPose.getRotation().plus(Rotation2d.fromDegrees(currentRotationAverage * headingAdjustmentFactorForSimulation)));
        pose.setCurrentPoseInMeters(updatedPose);

        // Let's also have a very simple physics mock for the arm and the shooter.
        // Get the power or setpoint for each arm.
        double powerToTicksRatio = 1;
        var leftMockMotor = ((MockCANSparkMax)arm.armMotorLeft);
        var rightMockMotor = ((MockCANSparkMax)arm.armMotorRight);

        leftMockMotor.setPosition(leftMockMotor.getPosition() +  (leftMockMotor.get() * powerToTicksRatio));
        rightMockMotor.setPosition(rightMockMotor.getPosition() +  (rightMockMotor.get() * powerToTicksRatio));

        // They might be using PID to control the arm. If so, we can use a moving aveage of their setpoint
        // to approximate internal PID.
        leftArmPositionCalculator.add(leftMockMotor.getReference());
        rightArmPositionCalculator.add(rightMockMotor.getReference());

        if (leftMockMotor.getControlType() == CANSparkBase.ControlType.kPosition) {
            leftMockMotor.setPosition(leftArmPositionCalculator.getAverage());
        }
        if (rightMockMotor.getControlType() == CANSparkBase.ControlType.kPosition) {
            rightMockMotor.setPosition(rightArmPositionCalculator.getAverage());
        }

        // simulate the arm limit switches being pressed when a certain position is reached
        if (leftMockMotor.getPosition() < -3) {
            leftMockMotor.setReverseLimitSwitchStateForTesting(true);
        } else {
            leftMockMotor.setReverseLimitSwitchStateForTesting(false);
        }

        if (rightMockMotor.getPosition() < -3) {
            rightMockMotor.setReverseLimitSwitchStateForTesting(true);
        } else {
            rightMockMotor.setReverseLimitSwitchStateForTesting(false);
        }

        // The shooter wheel should pretty much always be in velocity mode.
        var shooterUpperMockMotor = (MockCANSparkMax)shooter.upperWheelMotor;
        var shooterLowerMockMotor = (MockCANSparkMax)shooter.lowerWheelMotor;

        if (shooterUpperMockMotor.getControlType() == CANSparkBase.ControlType.kVelocity) {
            shooterVelocityCalculator.add(shooterUpperMockMotor.getReference());
        } else {
            shooterVelocityCalculator.add(0.0);
        }

        shooterUpperMockMotor.setVelocity(shooterVelocityCalculator.getAverage());
        shooterLowerMockMotor.setVelocity(shooterVelocityCalculator.getAverage());

        double currentCollectorPower = collector.collectorMotor.getAppliedOutput();
        if (currentCollectorPower == 1
            && lastCollectorPower == 0) {
            initializeNewFiredNote();
        }
        lastCollectorPower = currentCollectorPower;
        interpolateNotePosition();
    }

    double timeNoteFired = 0;
    Translation3d noteStartPoint;
    Translation3d noteFinishPoint;
    double lastCollectorPower = 0;

    private void initializeNewFiredNote() {
        timeNoteFired = XTimer.getFPGATimestamp();

        // get current position and angle
        var currentPose = pose.getCurrentPose2d();
        // create a translation3d representing note start point.
        noteStartPoint = new Translation3d(
                currentPose.getTranslation().getX()
                        + Math.cos(currentPose.getRotation().getRadians() + Math.PI) * 0.33,
                currentPose.getTranslation().getY()
                        + Math.sin(currentPose.getRotation().getRadians() + Math.PI) * 0.33,
                0.33
        );

        // create a translation3d representing note finish point.
        noteFinishPoint = new Translation3d(
                currentPose.getTranslation().getX()
                        + Math.cos(currentPose.getRotation().getRadians() + Math.PI) * 3,
                currentPose.getTranslation().getY()
                        + Math.sin(currentPose.getRotation().getRadians() + Math.PI) * 3,
                3
        );
    }

    /**
     * Interpolates the position of the note based on the time since it was fired.
     */
    private void interpolateNotePosition() {
        double timeSinceNoteFired = XTimer.getFPGATimestamp() - timeNoteFired;
        if (noteStartPoint != null && noteFinishPoint != null) {
            var interpolatedPosition = noteStartPoint.interpolate(noteFinishPoint, timeSinceNoteFired);
            Logger.recordOutput("VirtualNote", new Pose3d(interpolatedPosition, new Rotation3d()));
        }
    }
}
