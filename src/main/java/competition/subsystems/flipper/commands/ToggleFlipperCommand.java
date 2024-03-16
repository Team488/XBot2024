package competition.subsystems.flipper.commands;

import competition.subsystems.flipper.FlipperSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ToggleFlipperCommand extends BaseCommand {

    final FlipperSubsystem flipper;

    @Inject
    ToggleFlipperCommand(FlipperSubsystem flipper) {
        this.flipper = flipper;
    }

    @Override
    public void initialize() {
        flipper.toggleServo();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
