package competition.injection.modules;

import javax.inject.Singleton;

import xbot.common.injection.swerve.FrontLeftDrive;
import xbot.common.injection.swerve.FrontRightDrive;
import xbot.common.injection.swerve.RearLeftDrive;
import xbot.common.injection.swerve.RearRightDrive;
import xbot.common.injection.swerve.SwerveComponent;
import xbot.common.injection.swerve.SwerveInstance;
import competition.injection.components.BaseRobotComponent;
import competition.injection.components.CompetitionTestComponent;
import competition.injection.components.RobotComponent;
import competition.injection.components.SimulationComponent;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.oracle.NoteCollectionInfoSource;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(subcomponents = { SwerveComponent.class })
public class CommonModule {
    @Provides
    @Singleton
    public @FrontLeftDrive SwerveComponent frontLeftSwerveComponent(SwerveComponent.Builder builder) {
        return builder
                .swerveInstance(new SwerveInstance("FrontLeftDrive"))
                .build();
    }

    @Provides
    @Singleton
    public @FrontRightDrive SwerveComponent frontRightSwerveComponent(SwerveComponent.Builder builder) {
        return builder
                .swerveInstance(new SwerveInstance("FrontRightDrive"))
                .build();
    }

    @Provides
    @Singleton
    public @RearLeftDrive SwerveComponent rearLeftSwerveComponent(SwerveComponent.Builder builder) {
        return builder
                .swerveInstance(new SwerveInstance("RearLeftDrive"))
                .build();
    }

    @Provides
    @Singleton
    public @RearRightDrive SwerveComponent rearRightSwerveComponent(SwerveComponent.Builder builder) {
        return builder
                .swerveInstance(new SwerveInstance("RearRightDrive"))
                .build();
    }
}