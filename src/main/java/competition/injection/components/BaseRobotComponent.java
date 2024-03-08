package competition.injection.components;

import competition.auto_programs.SubwooferShotFromMidShootThenShootNearestThree;
import competition.simulation.Simulator2024;
import competition.subsystems.NeoTrellisGamepadSubsystem;
import competition.subsystems.arm.ArmModelBasedCalculator;
import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.ExtendArmCommand;
import competition.subsystems.arm.commands.ReconcileArmAlignmentCommand;
import competition.subsystems.arm.commands.RetractArmCommand;
import competition.subsystems.arm.commands.StopArmCommand;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.lights.LightSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.schoocher.ScoocherSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
import competition.subsystems.shooter.commands.ShooterWheelMaintainerCommand;
import xbot.common.injection.swerve.SwerveComponentHolder;
import competition.operator_interface.OperatorCommandMap;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.SubsystemDefaultCommandMap;
import competition.subsystems.oracle.ManualRobotKnowledgeSubsystem;
import competition.subsystems.oracle.NoteCollectionInfoSource;
import competition.subsystems.oracle.NoteFiringInfoSource;
import competition.subsystems.vision.VisionSubsystem;
import dagger.Binds;
import xbot.common.injection.components.BaseComponent;
import xbot.common.subsystems.drive.swerve.SwerveDefaultCommandMap;

public abstract class BaseRobotComponent extends BaseComponent {

    public abstract SubsystemDefaultCommandMap subsystemDefaultCommandMap();

    public abstract SwerveDefaultCommandMap swerveDefaultCommandMap();

    public abstract OperatorCommandMap operatorCommandMap();

    public abstract OperatorInterface operatorInterface();

    public abstract SwerveComponentHolder swerveComponents();

    public abstract VisionSubsystem visionSubsystem();

    public abstract ArmSubsystem armSubsystem();
    public abstract ExtendArmCommand extendArmCommand();
    public abstract RetractArmCommand retractArmCommand();
    public abstract StopArmCommand stopArmCommand();
    public abstract ReconcileArmAlignmentCommand reconcileArmAlignmentCommand();

    public abstract NeoTrellisGamepadSubsystem neoTrellisGamepadSubsystem();

    public abstract ScoocherSubsystem scoocherSubsystem();

    public abstract CollectorSubsystem collectorSubsystem();

    public abstract ShooterWheelSubsystem shooterSubsystem();

    public abstract LightSubsystem lightSubsystem();

    public abstract Simulator2024 simulator2024();
    public abstract ShooterWheelMaintainerCommand shooterWheelMaintainerCommand();
    public abstract DynamicOracle dynamicOracle();
    public abstract ArmModelBasedCalculator armModelBasedCalculator();
    public abstract SubwooferShotFromMidShootThenShootNearestThree subwooferShotFromMidShootThenShootNearestThree();
}