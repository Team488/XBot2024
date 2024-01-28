package competition.injection.components;

import javax.inject.Singleton;

import competition.injection.modules.CommonModule;
import competition.injection.modules.CommonInterfaceBindingModule;
import competition.injection.modules.SimulatedRobotModule;
import competition.injection.modules.UnitTestRobotModule;
import dagger.Component;
import xbot.common.injection.modules.MockControlsModule;
import xbot.common.injection.modules.MockDevicesModule;
import xbot.common.injection.modules.UnitTestModule;

@Singleton
@Component(modules = { UnitTestModule.class, MockDevicesModule.class, MockControlsModule.class,
        UnitTestRobotModule.class, CommonModule.class, CommonInterfaceBindingModule.class })
public abstract class CompetitionTestComponent extends BaseRobotComponent {
    
}
