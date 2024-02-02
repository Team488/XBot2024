package competition.injection.components;

import competition.subsystems.arm.ArmSubsystem;
import competition.subsystems.arm.commands.ExtendArmCommand;
import competition.subsystems.arm.commands.ReconcileArmAlignmentCommand;
import competition.subsystems.arm.commands.RetractArmCommand;
import competition.subsystems.arm.commands.StopArmCommand;
import competition.subsystems.collector.CollectorSubsystem;
import competition.subsystems.schoocher.ScoocherSubsystem;
import competition.subsystems.shooter.ShooterWheelSubsystem;
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

    public abstract ScoocherSubsystem scoocherSubsystem();

    public abstract CollectorSubsystem collectorSubsystem();

    public abstract ShooterWheelSubsystem shooterSubsystem();
}