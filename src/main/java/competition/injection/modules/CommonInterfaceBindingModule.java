package competition.injection.modules;

import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.oracle.NoteCollectionInfoSource;
import competition.subsystems.oracle.NoteFiringInfoSource;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class CommonInterfaceBindingModule {
    @Binds
    abstract NoteCollectionInfoSource manualRobotKnowledgeSubsystemCollection(CollectorSubsystem impl);
    @Binds
    abstract NoteFiringInfoSource manualRobotKnowledgeSubsystemFiring(CollectorSubsystem impl);
}
