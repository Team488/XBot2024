package competition.subsystems.flipper.commands;

import competition.subsystems.flipper.FlipperSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class SetFlipperServoToHangPositionCommand extends BaseCommand {
    final FlipperSubsystem flipper;

    @Inject
    SetFlipperServoToHangPositionCommand(FlipperSubsystem flipper) {
        addRequirements(flipper);
        this.flipper = flipper;
    }
    @Override
    public void initialize() {
        flipper.flipperServoHangingPosition();
    }

    @Override
    public boolean isFinished() {
        // Question: Is this kinda dangerous since there is no going back once pressed?
        // (Or maybe that it can only be used once)
        return false;
    }
}
