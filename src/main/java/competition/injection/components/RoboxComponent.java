package competition.injection.components;

import competition.injection.modules.CommonModule;
import competition.injection.modules.CommonInterfaceBindingModule;
import competition.injection.modules.RoboxModule;
import dagger.Component;
import xbot.common.injection.modules.RealControlsModule;
import xbot.common.injection.modules.RealDevicesModule;
import xbot.common.injection.modules.RobotModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = { RobotModule.class, RealDevicesModule.class, RealControlsModule.class,
        RoboxModule.class, CommonModule.class, CommonInterfaceBindingModule.class })
public abstract class RoboxComponent extends BaseRobotComponent {
    
}
