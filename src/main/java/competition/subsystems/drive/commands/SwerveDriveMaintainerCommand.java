package competition.subsystems.drive.commands;

import javax.inject.Inject;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.subsystems.drive.swerve.SwerveDriveSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider.HumanVsMachineDeciderFactory;
import xbot.common.properties.PropertyFactory;

public class SwerveDriveMaintainerCommand extends BaseMaintainerCommand<Double> {

    private final SwerveDriveSubsystem subsystem;
    private final DriveSubsystem drive;

    @Inject
    public SwerveDriveMaintainerCommand(DriveSubsystem drive, SwerveDriveSubsystem subsystemToMaintain,
                                        PropertyFactory pf, HumanVsMachineDeciderFactory hvmFactory) {
        super(subsystemToMaintain, pf, hvmFactory, 0.001, 0.001);
        this.subsystem = subsystemToMaintain;
        this.drive = drive;
    }

    @Override
    protected void coastAction() {
        this.subsystem.setPower(0.0);
    }

    @Override
    protected void calibratedMachineControlAction() {
        // The drive subsystem is setting velocity goals, but we're starting simple.
        // Just set % power by dividing by the max allowable velocity.
        if (drive.getMaxTargetSpeedInchesPerSecond() > 0) {
            this.subsystem.setPower(this.subsystem.getTargetValue() / drive.getMaxTargetSpeedInchesPerSecond());
        } else {
            this.subsystem.setPower(0.0);
        }
    }

    @Override
    protected double getErrorMagnitude() {
        return Math.abs((this.subsystem.getTargetValue() - this.subsystem.getCurrentValue()));
    }

    @Override
    protected Double getHumanInput() {
        // never hooked directly to human input, human input handled by drive
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return getHumanInput();
    }

    @Override
    public void initialize() {
        this.subsystem.setTargetValue(0.0);
        this.subsystem.setPower(0.0);
        //this.subsystem.resetPid();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);

        this.initialize();
    }
}