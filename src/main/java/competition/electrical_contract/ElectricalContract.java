package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.math.XYPair;

public abstract class ElectricalContract
        implements XCameraElectricalContract, XSwerveDriveElectricalContract {
    public abstract boolean isShooterReady();

    public abstract boolean isDriveReady();

    public abstract boolean areCanCodersReady();

    public abstract DeviceInfo getDriveMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance);

    public abstract XYPair getSwerveModuleOffsets(SwerveInstance swerveInstance);

    public abstract boolean isCollectorReady();

    public abstract boolean isScoocherReady();
    public abstract DeviceInfo getScoocherMotor();

    public abstract DeviceInfo getLightsDio0();

    public abstract DeviceInfo getLightsDio1();

    public abstract DeviceInfo getLightsDio2();

    public abstract DeviceInfo getLightsDio3();

    public abstract DeviceInfo getLightsDio4();

    public abstract DeviceInfo getLightsCubeDio();

    public abstract DeviceInfo getInControlNoteSensorDio();
    public abstract DeviceInfo getReadyToFireNoteSensorDio();

    // ArmSubsystem
    public abstract boolean isArmReady();
    public abstract DeviceInfo getArmMotorLeft();

    public abstract DeviceInfo getArmMotorRight();
    public abstract DeviceInfo getBrakeSolenoidForward();
    public abstract DeviceInfo getBrakeSolenoidReverse();


    public abstract boolean getArmEncoderInverted();
    public abstract boolean getArmEncoderIsOnLeftMotor();

    public abstract DeviceInfo getCollectorMotor();


    // ShooterSubsystem
    public abstract DeviceInfo getShooterMotorLeader();

    public abstract DeviceInfo getShooterMotorFollower();

    // Vision
    public abstract CameraInfo[] getAprilTagCameraInfo();

    public abstract CameraInfo[] getNoteCameraInfo();
}






