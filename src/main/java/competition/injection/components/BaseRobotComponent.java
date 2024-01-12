package competition.injection.components;

import competition.operator_interface.OperatorCommandMap;
import competition.subsystems.SubsystemDefaultCommandMap;
import xbot.common.injection.components.BaseComponent;

public abstract class BaseRobotComponent extends BaseComponent {
    public abstract SubsystemDefaultCommandMap subsystemDefaultCommandMap();

    public abstract OperatorCommandMap operatorCommandMap();
}
