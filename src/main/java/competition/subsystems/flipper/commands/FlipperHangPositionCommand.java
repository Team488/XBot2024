package competition.subsystems.flipper.commands;

import competition.subsystems.flipper.FlipperSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FlipperHangPositionCommand extends BaseCommand {
    final FlipperSubsystem flipper;

    @Inject
    FlipperHangPositionCommand(FlipperSubsystem flipper) {
        this.flipper = flipper;
    }


    @Override
    public void initialize() {
        flipper.servoHangingPosition();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
