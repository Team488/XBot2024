package competition.injection.components;

import xbot.common.injection.swerve.SwerveComponentHolder;
import competition.operator_interface.OperatorCommandMap;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.SubsystemDefaultCommandMap;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.oracle.NoteCollectionInfoSource;
import competition.subsystems.oracle.NoteFiringInfoSource;
import dagger.Binds;
import xbot.common.injection.components.BaseComponent;

public abstract class BaseRobotComponent extends BaseComponent {

    public abstract SubsystemDefaultCommandMap subsystemDefaultCommandMap();

    public abstract OperatorCommandMap operatorCommandMap();

    public abstract OperatorInterface operatorInterface();

    public abstract SwerveComponentHolder swerveComponents();
}