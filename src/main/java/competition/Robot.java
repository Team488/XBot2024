
package competition;

import competition.injection.components.BaseRobotComponent;
import competition.injection.components.DaggerPracticeRobotComponent;
import competition.injection.components.DaggerRobotComponent;
import competition.injection.components.DaggerRoboxComponent;
import competition.injection.components.DaggerSimulationComponent;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import xbot.common.command.BaseRobot;
import xbot.common.math.FieldPose;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import java.util.LinkedList;
import java.util.Queue;

public class Robot extends BaseRobot {

    @Override
    protected void initializeSystems() {
        super.initializeSystems();
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().swerveDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();

        dataFrameRefreshables.add((DriveSubsystem)getInjectorComponent().driveSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().poseSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().visionSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().armSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().scoocherSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().collectorSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().shooterSubsystem());
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

    @Override
    public void simulationPeriodic() {
        super.simulationPeriodic();

        double robotTopSpeedInMetersPerSecond = 3.0;
        double robotLoopPeriod = 0.02;
        double poseAdjustmentFactorForSimulation = robotTopSpeedInMetersPerSecond * robotLoopPeriod;

        double robotTopAngularSpeedInDegreesPerSecond = 360;
        double headingAdjustmentFactorForSimulation = robotTopAngularSpeedInDegreesPerSecond * robotLoopPeriod;

        var pose = (PoseSubsystem)getInjectorComponent().poseSubsystem();
        var currentPose = pose.getCurrentPose2d();
        DriveSubsystem drive = (DriveSubsystem)getInjectorComponent().driveSubsystem();

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
    }

    public class MovingAverage<T> {
        private static final int SIZE = 15;
        private Queue<T> queue;
        private T sum;
        private SumFunction<T> sumFunction;

        public MovingAverage(SumFunction<T> sumFunction, T initialValue) {
            this.queue = new LinkedList<>();
            this.sum = initialValue;
            this.sumFunction = sumFunction;
        }

        public void add(T value) {
            sum = sumFunction.add(sum, value);
            queue.add(value);
            if (queue.size() > SIZE) {
                sum = sumFunction.subtract(sum, queue.remove());
            }
        }

        public T getAverage() {
            if (queue.isEmpty()) {
                return sum;
            }
            return sumFunction.divide(sum, queue.size());
        }

        public interface SumFunction<T> {
            T add(T a, T b);
            T subtract(T a, T b);
            T divide(T a, int b);
        }
    }

    MovingAverage<Translation2d> translationAverageCalculator = new MovingAverage<>(
            new MovingAverage.SumFunction<Translation2d>() {
                @Override
                public Translation2d add(Translation2d a, Translation2d b) {
                    return a.plus(b);
                }

                @Override
                public Translation2d subtract(Translation2d a, Translation2d b) {
                    return a.minus(b);
                }

                @Override
                public Translation2d divide(Translation2d a, int b) {
                    return new Translation2d(a.getX() / b, a.getY() / b);
                }
            },
            new Translation2d()
    );

    MovingAverage<Double> rotationAverageCalculator = new MovingAverage<>(
            new MovingAverage.SumFunction<Double>() {
                @Override
                public Double add(Double a, Double b) {
                    return a + b;
                }

                @Override
                public Double subtract(Double a, Double b) {
                    return a - b;
                }

                @Override
                public Double divide(Double a, int b) {
                    return a / b;
                }
            },
            0.0
    );
}
