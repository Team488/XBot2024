package competition;

import competition.subsystems.drive.DriveSubsystem;
import org.junit.Test;

public class RobotInitTest extends BaseCompetitionTest {
    @Test
    public void testDefaultSystem() {
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();
    }

    @Test
    public void testDataFrameRefreshes() {
        ((DriveSubsystem)(getInjectorComponent().driveSubsystem())).refreshDataFrame();
        getInjectorComponent().shooterSubsystem().refreshDataFrame();
        getInjectorComponent().collectorSubsystem().refreshDataFrame();
        getInjectorComponent().scoocherSubsystem().refreshDataFrame();
        getInjectorComponent().armSubsystem().refreshDataFrame();
    }

    @Test
    public void testPeriodics() {
        getInjectorComponent().driveSubsystem().periodic();
        getInjectorComponent().shooterSubsystem().periodic();
        getInjectorComponent().collectorSubsystem().periodic();
        getInjectorComponent().scoocherSubsystem().periodic();
        getInjectorComponent().lightSubsystem().periodic();
        getInjectorComponent().armSubsystem().periodic();
        getInjectorComponent().dynamicOracle().periodic();
    }
}

