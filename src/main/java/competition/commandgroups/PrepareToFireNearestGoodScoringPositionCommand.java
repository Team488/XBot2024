package competition.commandgroups;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PointOfInterest;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class PrepareToFireNearestGoodScoringPositionCommand extends BaseSetpointCommand {
    ArmSubsystem arm;
    DynamicOracle oracle;
    ShooterWheelSubsystem shooter;

    @Inject
    public PrepareToFireNearestGoodScoringPositionCommand(ArmSubsystem arm, DynamicOracle oracle,
                                                          ShooterWheelSubsystem shooter) {
        super(shooter);
        this.arm = arm;
        this.oracle = oracle;
        this.shooter = shooter;
    }

    public void initialize() {
        
    }

    @Override
    public void execute() {
        PointOfInterest nearestScoringLocation = oracle.getNearestScoringLocation();
        shooter.setTargetValue(shooter.getRPMForGivenScoringLocation(nearestScoringLocation));
        arm.setTargetValue(arm.getUsefulArmPositionExtensionInMm(nearestScoringLocation));
    }

    @Override
    public boolean isFinished() {
        return false;
    }

}