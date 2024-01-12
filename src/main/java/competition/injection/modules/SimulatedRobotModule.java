package competition.injection.modules;

import javax.inject.Singleton;

import competition.electrical_contract.CompetitionContract;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import dagger.Binds;
import dagger.Module;
import xbot.common.subsystems.drive.BaseDriveSubsystem;
import xbot.common.subsystems.pose.BasePoseSubsystem;

@Module
public abstract class SimulatedRobotModule {
    @Binds
    @Singleton
    public abstract ElectricalContract getElectricalContract(CompetitionContract impl);

    @Binds
    @Singleton
    public abstract BasePoseSubsystem getPoseSubsystem(PoseSubsystem impl);

    @Binds
    @Singleton
    public abstract BaseDriveSubsystem getDriveSubsystem(DriveSubsystem impl);
}
