package competition.subsystems.drive;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.injection.swerve.FrontLeftDrive;
import xbot.common.injection.swerve.FrontRightDrive;
import xbot.common.injection.swerve.RearLeftDrive;
import xbot.common.injection.swerve.RearRightDrive;
import xbot.common.injection.swerve.SwerveComponent;
import xbot.common.math.PIDDefaults;
import xbot.common.math.PIDManager;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Supplier;

@Singleton
public class DriveSubsystem extends BaseSwerveDriveSubsystem implements DataFrameRefreshable {
    private static final Logger log = LogManager.getLogger(DriveSubsystem.class);

    private Pose2d targetNote;

    private boolean specialHeadingTargetActive = false;
    private boolean specialPointAtPositionTargetActive = false;
    private Rotation2d specialHeadingTarget = new Rotation2d();
    private Translation2d specialPointAtPositionTarget = new Translation2d();
    private final DoubleProperty suggestedAutonomousMaximumSpeed;
    private final DoubleProperty suggestedAutonomousExtremeSpeed;
    private final DoubleProperty suggestedAutonomousSpeedIfNotVisionAssisted;
    private final PIDManager aggressiveGoalHeadingPidManager;

    @Inject
    public DriveSubsystem(PIDManagerFactory pidFactory, PropertyFactory pf,
                          @FrontLeftDrive SwerveComponent frontLeftSwerve, @FrontRightDrive SwerveComponent frontRightSwerve,
                          @RearLeftDrive SwerveComponent rearLeftSwerve, @RearRightDrive SwerveComponent rearRightSwerve,
                          PIDManagerFactory aggressiveGoalHeadingPidFactory) {
        super(pidFactory, pf, frontLeftSwerve, frontRightSwerve, rearLeftSwerve, rearRightSwerve);
        log.info("Creating DriveSubsystem");

        pf.setPrefix(this.getPrefix());
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        suggestedAutonomousMaximumSpeed =
                pf.createPersistentProperty("Suggested Autonomous Maximum Speed", 3.0);
        suggestedAutonomousExtremeSpeed =
                pf.createPersistentProperty("Suggested Autonomous EXTREME Speed", 5.0);
        suggestedAutonomousSpeedIfNotVisionAssisted =
                pf.createPersistentProperty("Suggested Autonomous NO VISION Speed", 1.5);

        aggressiveGoalHeadingPidManager = aggressiveGoalHeadingPidFactory.create(
                this.getPrefix() + "AggressiveGoalHeadingPID",
                new PIDDefaults(
                        0.01, // P
                        0.000001, // I
                        0.02, // D
                        0.0, // F
                        0.75, // Max output
                        -0.75, // Min output
                        2.0, // Error threshold
                        0.2, // Derivative threshold
                        0.2) // Time threshold
        );
        aggressiveGoalHeadingPidManager.setEnableErrorThreshold(true);
        aggressiveGoalHeadingPidManager.setEnableTimeThreshold(true);
    }

    public double getSuggestedAutonomousMaximumSpeed() {
        return suggestedAutonomousMaximumSpeed.get();
    }

    public double getSuggestedAutonomousExtremeSpeed() { return suggestedAutonomousExtremeSpeed.get(); }
    public double getSuggestedAutonomousNoAutoSpeed() {
        return suggestedAutonomousSpeedIfNotVisionAssisted.get();
    }

    @Override
    protected PIDDefaults getPositionalPIDDefaults() {
        return super.getPositionalPIDDefaults();
    }

    @Override
    protected PIDDefaults getHeadingPIDDefaults() {
        return super.getHeadingPIDDefaults();
    }

    public void setTargetNote(Pose2d targetNote) {
        this.targetNote = targetNote;
    }

    public Pose2d getTargetNote() {
        return targetNote;
    }

    public void setSpecialHeadingTarget(Rotation2d specialHeadingTarget) {
        this.specialHeadingTarget = specialHeadingTarget;
    }

    public Rotation2d getSpecialHeadingTarget() {
        return specialHeadingTarget;
    }

    public void setSpecialHeadingTargetActive(boolean specialHeadingTargetActive) {
        this.specialHeadingTargetActive = specialHeadingTargetActive;
    }

    public boolean isSpecialHeadingTargetActive() {
        return specialHeadingTargetActive;
    }

    public void setSpecialPointAtPositionTarget(Translation2d specialPointAtPositionTarget) {
        this.specialPointAtPositionTarget = specialPointAtPositionTarget;
    }

    public Translation2d getSpecialPointAtPositionTarget() {
        return specialPointAtPositionTarget;
    }

    public void setSpecialPointAtPositionTargetActive(boolean specialPointAtPositionTargetActive) {
        this.specialPointAtPositionTargetActive = specialPointAtPositionTargetActive;
    }

    public boolean isSpecialPointAtPositionTargetActive() {
        return specialPointAtPositionTargetActive;
    }

    public InstantCommand createSetSpecialHeadingTargetCommand(Supplier<Rotation2d> specialHeadingTarget) {
        return new InstantCommand(() -> {
            setSpecialHeadingTarget(specialHeadingTarget.get());
            setSpecialHeadingTargetActive(true);}
        );
    }

    public InstantCommand createSetSpecialPointAtPositionTargetCommand(Supplier<Translation2d> specialPointAtPositionTarget) {
        return new InstantCommand(() -> {
            setSpecialPointAtPositionTarget(specialPointAtPositionTarget.get());
            setSpecialPointAtPositionTargetActive(true);}
        );
    }

    public InstantCommand createClearAllSpecialTargetsCommand() {
        return new InstantCommand(() -> {
            setSpecialHeadingTargetActive(false);
            setSpecialPointAtPositionTargetActive(false);
        });
    }

    public PIDManager getAggressiveGoalHeadingPid() {
        return aggressiveGoalHeadingPidManager;
    }


    @Override
    public boolean getStaticHeadingActive() {
        return false;
    }

    @Override
    public boolean getLookAtPointActive() {
        return false;
    }

    @Override
    public Rotation2d getStaticHeadingTarget() {
        return null;
    }

    @Override
    public Translation2d getLookAtPointTarget() {
        return null;
    }
}