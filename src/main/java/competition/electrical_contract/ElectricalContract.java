package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANTalonInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.math.XYPair;

public abstract class ElectricalContract implements XSwerveDriveElectricalContract {
    public abstract boolean isDriveReady();

    public abstract boolean areCanCodersReady();

    public abstract DeviceInfo getDriveMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance);

    public abstract XYPair getSwerveModuleOffsets(SwerveInstance swerveInstance);

    public abstract DeviceInfo getCollectorSolenoid();
    public abstract DeviceInfo getCollectorMotor();
    public abstract boolean isCollectorReady();

    public abstract DeviceInfo getLightsDio0();
    public abstract DeviceInfo getLightsDio1();
    public abstract DeviceInfo getLightsDio2();
    public abstract DeviceInfo getLightsDio3();
    public abstract DeviceInfo getLightsDio4();
    public abstract DeviceInfo getLightsCubeDio();
}
