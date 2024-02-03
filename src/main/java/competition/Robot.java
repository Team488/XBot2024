
package competition;

import com.revrobotics.CANSparkBase;
import competition.injection.components.BaseRobotComponent;
import competition.injection.components.DaggerPracticeRobotComponent;
import competition.injection.components.DaggerRobotComponent;
import competition.injection.components.DaggerRoboxComponent;
import competition.injection.components.DaggerSimulationComponent;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import xbot.common.command.BaseRobot;
import xbot.common.controls.actuators.mock_adapters.MockCANSparkMax;
import xbot.common.math.FieldPose;
import xbot.common.math.MovingAverage;
import xbot.common.math.MovingAverageForDouble;
import xbot.common.math.MovingAverageForTranslation2d;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import java.util.LinkedList;
import java.util.Queue;

public class Robot extends BaseRobot {

    public Robot() {
    }

    PoseSubsystem pose;
    DriveSubsystem drive;
    ArmSubsystem arm;
    ShooterWheelSubsystem shooter;

    @Override
    protected void initializeSystems() {
        super.initializeSystems();
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().swerveDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();

        this.pose = (PoseSubsystem)getInjectorComponent().poseSubsystem();
        this.drive = (DriveSubsystem)getInjectorComponent().driveSubsystem();
        this.arm = getInjectorComponent().armSubsystem();
        this.shooter = getInjectorComponent().shooterSubsystem();

        dataFrameRefreshables.add((DriveSubsystem)getInjectorComponent().driveSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().poseSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().visionSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().armSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().scoocherSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().collectorSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().shooterSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().neoTrellisGamepadSubsystem());
    }

    protected BaseRobotComponent createDaggerComponent() {
        if (BaseRobot.isReal()) {
            // Initialize the contract to use if this is a fresh robot. Assume competition since that's the safest.
            if (!Preferences.containsKey("ContractToUse")) {
                Preferences.setString("ContractToUse", "Competition");
            }

            String chosenContract = Preferences.getString("ContractToUse", "Competition");

            switch (chosenContract) {
                case "Practice":
                    System.out.println("Using practice contract");
                    return DaggerPracticeRobotComponent.create();
                case "Robox":
                    System.out.println("Using Robox contract");
                    return DaggerRoboxComponent.create();
                default:
                    System.out.println("Using Competition contract");
                    // In all other cases, return the competition component.
                    return DaggerRobotComponent.create();
            }
        } else {
            return DaggerSimulationComponent.create();
        }
    }

    public BaseRobotComponent getInjectorComponent() {
        return (BaseRobotComponent)super.getInjectorComponent();
    }

    @Override
    public void simulationInit() {
        super.simulationInit();
        // Automatically enables the robot; remove this line of code if you want the robot
        // to start in a disabled state (as it would on the field). However, this does save you the 
        // hassle of navigating to the DS window and re-enabling the simulated robot.
        DriverStationSim.setEnabled(true);
        //webots.setFieldPoseOffset(getFieldOrigin());
        DriverStationSim.setAllianceStationId(AllianceStationID.Blue1);
    }

    private FieldPose getFieldOrigin() {
        // Modify this to whatever the simulator coordinates are for the "FRC origin" of the field.
        // From a birds-eye view where your alliance station is at the bottom, this is the bottom-left corner
        // of the field.
        return new FieldPose(
            -2.33*PoseSubsystem.INCHES_IN_A_METER, 
            -4.58*PoseSubsystem.INCHES_IN_A_METER, 
            BasePoseSubsystem.FACING_TOWARDS_DRIVERS
            );
    }

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

    @Override
    public void simulationPeriodic() {
        super.simulationPeriodic();

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

        // The shooter wheel should pretty much always be in velocity mode.
        var shooterMockMotor = (MockCANSparkMax)shooter.leader;
        shooterVelocityCalculator.add(shooterMockMotor.getReference());
        if (shooterMockMotor.getControlType() == CANSparkBase.ControlType.kVelocity) {
            shooterMockMotor.setVelocity(shooterVelocityCalculator.getAverage());
        }
    }
}

