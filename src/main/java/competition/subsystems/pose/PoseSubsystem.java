package competition.subsystems.pose;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.controls.sensors.XGyro.XGyroFactory;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.pose.BasePoseSubsystem;

@Singleton
public class PoseSubsystem extends BasePoseSubsystem {

    private final DriveSubsystem drive;

    @Inject
    public PoseSubsystem(XGyroFactory gyroFactory, PropertyFactory propManager, DriveSubsystem drive) {
        super(gyroFactory, propManager);
        this.drive = drive;
    }


    @Override
    protected double getLeftDriveDistance() {
        return drive.getLeftTotalDistance();
    }

    @Override
    protected double getRightDriveDistance() {
        return drive.getRightTotalDistance();
    }

}