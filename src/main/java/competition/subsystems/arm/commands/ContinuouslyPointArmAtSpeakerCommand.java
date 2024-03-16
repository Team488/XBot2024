package competition.subsystems.arm.commands;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class ContinuouslyPointArmAtSpeakerCommand extends BaseSetpointCommand {

    ArmSubsystem arm;
    PoseSubsystem pose;

    @Inject
    public ContinuouslyPointArmAtSpeakerCommand(ArmSubsystem arm, PoseSubsystem pose) {
        super(arm);
        this.arm = arm;
        this.pose = pose;
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        arm.setTargetValue(arm.getModeledExtensionForGivenSpeakerDistance(pose.getDistanceFromSpeaker()));
    }
}
