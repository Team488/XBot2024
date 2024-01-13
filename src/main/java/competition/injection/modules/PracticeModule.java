package competition.injection.modules;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.PracticeContract;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import dagger.Binds;
import dagger.Module;
import xbot.common.subsystems.drive.BaseDriveSubsystem;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import javax.inject.Singleton;

@Module
public abstract class PracticeModule {
    @Binds
    @Singleton
    public abstract ElectricalContract getElectricalContract(PracticeContract impl);

    @Binds
    @Singleton
    public abstract BasePoseSubsystem getPoseSubsystem(PoseSubsystem impl);

    @Binds
    @Singleton
    public abstract BaseDriveSubsystem getDriveSubsystem(DriveSubsystem impl);
}
