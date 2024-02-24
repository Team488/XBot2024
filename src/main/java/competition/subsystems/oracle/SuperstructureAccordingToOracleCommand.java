package competition.subsystems.oracle;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class SuperstructureAccordingToOracleCommand extends BaseCommand {

    DynamicOracle oracle;
    ArmSubsystem arm;
    ShooterWheelSubsystem shooter;
    CollectorSubsystem collector;

    @Inject
    public SuperstructureAccordingToOracleCommand(ArmSubsystem arm, ShooterWheelSubsystem shooter, DynamicOracle oracle, CollectorSubsystem collector) {
        this.oracle = oracle;
        this.arm = arm;
        this.shooter = shooter;
        this.collector = collector;

        // Take control of the targets for arm and shooter
        addRequirements(arm.getSetpointLock(), shooter.getSetpointLock(), collector);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
    }

    @Override
    public void execute() {
        // Basically, listen to the oracle.
        boolean tryToScore = false;
        switch (oracle.getHighLevelGoal()) {
            case CollectNote -> {
                collector.intake();
                arm.setTargetValue(0.0);
                tryToScore = false;
            }
            case ScoreInAmp -> {
                arm.setTargetValue(arm.getUsefulArmPositionExtensionInMm(ArmSubsystem.UsefulArmPosition.FIRING_FROM_AMP));
                tryToScore = true;
            }
            case ScoreInSpeaker -> {
                shooter.setTargetValue(shooter.getSpeedForRange());
                arm.setTargetValue(arm.getRecommendedExtensionForSpeaker());
                tryToScore = true;

            }
            default -> {
                collector.stop();
                arm.setTargetValue(0.0);
                tryToScore = false;
            }
        }

        if (tryToScore) {
            if (oracle.getScoringSubgoal() == DynamicOracle.ScoringSubGoals.EarnestlyLaunchNote) {
                fireWhenReady();
            } else {
                // probably driving to goal now
                collector.stop();
            }
        }
    }

    private void fireWhenReady() {
        if (getShouldCommitToFiring() && oracle.getHighLevelGoal() != DynamicOracle.HighLevelGoal.CollectNote) {
            collector.fire();
        } else {
            collector.stop();
        }
    }

    private boolean getShouldCommitToFiring() {
        return collector.getIntakeState() == CollectorSubsystem.IntakeState.FIRING
                || (shooter.isMaintainerAtGoal() && arm.isMaintainerAtGoal());
    }
}