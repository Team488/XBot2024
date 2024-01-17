package competition.injection.modules;

import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.oracle.NoteCollectionInfoSource;
import competition.subsystems.oracle.NoteFiringInfoSource;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class CommonInterfaceBindingModule {
    @Binds
    abstract NoteCollectionInfoSource manualRobotKnowledgeSubsystemCollection(ManualRobotKnowledgeSubsystem impl);
    @Binds
    abstract NoteFiringInfoSource manualRobotKnowledgeSubsystemFiring(ManualRobotKnowledgeSubsystem impl);
}
