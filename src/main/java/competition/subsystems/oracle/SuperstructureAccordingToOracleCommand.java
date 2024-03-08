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
    DynamicOracle.HighLevelGoal lastGoal = DynamicOracle.HighLevelGoal.NoGoal;

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
                if (lastGoal != DynamicOracle.HighLevelGoal.CollectNote) {
                    collector.resetCollectionState();
                }
                collector.intake();
                arm.setTargetValue(0.0);
                tryToScore = false;
            }
            case ScoreInAmp -> {
                arm.setTargetValue(ArmSubsystem.UsefulArmPosition.FIRING_FROM_AMP);
                tryToScore = true;
            }
            case ScoreInSpeaker -> {
                shooter.setTargetValue(shooter.getRPMForGivenScoringLocation(oracle.getChosenScoringLocation()));
                arm.setTargetValue(arm.getUsefulArmPositionExtensionInMm(oracle.getChosenScoringLocation()));
                tryToScore = true;
            }
            default -> {
                collector.stop();
                arm.setTargetValue(0.0);
                tryToScore = false;
            }
        }

        if (tryToScore) {
            // If it's time to score, or if we're already started firing, commit! (Or should this live in the collector?)
            if (oracle.getScoringSubgoal() == DynamicOracle.ScoringSubGoals.EarnestlyLaunchNote) {
                fireWhenReady();
            } else {
                // probably driving to goal now
                collector.stop();
            }
        }

        lastGoal = oracle.getHighLevelGoal();
    }

    private void fireWhenReady() {

        boolean superStructureReady = arm.isMaintainerAtGoal() && shooter.hasNonIdleTarget() && shooter.isMaintainerAtGoal();
        boolean sanityChecks = oracle.getHighLevelGoal() != DynamicOracle.HighLevelGoal.CollectNote;

        aKitLog.record("SuperstructureReady", superStructureReady);
        aKitLog.record("SanityChecks", sanityChecks);


        if (superStructureReady && sanityChecks) {
            collector.fire();
        } else {
            collector.stop();
        }
    }
}
