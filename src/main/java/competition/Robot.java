
package competition;

import competition.injection.components.BaseRobotComponent;
import competition.injection.components.DaggerPracticeRobotComponent;
import competition.injection.components.DaggerRobotComponent;
import competition.injection.components.DaggerRoboxComponent;
import competition.injection.components.DaggerSimulationComponent;
import competition.simulation.Simulator2024;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.littletonrobotics.junction.LogTable;
import xbot.common.command.BaseRobot;
import xbot.common.math.FieldPose;
import xbot.common.math.MovingAverageForDouble;
import xbot.common.math.MovingAverageForTranslation2d;
import xbot.common.subsystems.pose.BasePoseSubsystem;

public class Robot extends BaseRobot {

    public Robot() {
    }

    Simulator2024 simulator;
    DynamicOracle oracle;
    PoseSubsystem poseSubsystem;

    @Override
    protected void initializeSystems() {
        super.initializeSystems();
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().swerveDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();
        getInjectorComponent().lightSubsystem();
        getInjectorComponent().flipperSubsystem();

        if (BaseRobot.isSimulation()) {
            simulator = getInjectorComponent().simulator2024();
        }
        oracle = getInjectorComponent().dynamicOracle();

        dataFrameRefreshables.add((DriveSubsystem)getInjectorComponent().driveSubsystem());
        poseSubsystem = (PoseSubsystem) getInjectorComponent().poseSubsystem();
        dataFrameRefreshables.add(poseSubsystem);
        dataFrameRefreshables.add(getInjectorComponent().visionSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().armSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().scoocherSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().collectorSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().shooterSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().neoTrellisGamepadSubsystem());
        var defaultAuto = getInjectorComponent().subwooferShotFromMidShootThenShootNearestThree();
        var autoSelector = getInjectorComponent().autonomousCommandSelector();

        autoSelector.setCurrentAutonomousCommand(defaultAuto);
        autoSelector.setIsDefault(true);
        LogTable.disableProtobufWarning();
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
    public void autonomousInit() {
        oracle.freezeConfigurationForAutonomous();
        oracle.requestReevaluation();
        super.autonomousInit();
    }

    @Override
    public void teleopInit() {
        super.teleopInit();
        oracle.clearNoteMapForTeleop();
        oracle.clearScoringLocationsForTeleop();
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

        if (simulator != null) {
           simulator.update();
        }
    }
}

