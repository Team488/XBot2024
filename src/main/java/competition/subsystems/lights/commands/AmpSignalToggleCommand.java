package competition.subsystems.lights.commands;

import competition.subsystems.lights.LightSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class AmpSignalToggleCommand extends BaseCommand {

    LightSubsystem light;

    @Inject
    AmpSignalToggleCommand(LightSubsystem light) {
        this.light = light;
    }

    @Override
    public void initialize() {
        light.toggleAmpSignal();
    }

    @Override
    public void execute() {
        // Empty
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
