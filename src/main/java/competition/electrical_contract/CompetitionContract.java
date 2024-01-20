package competition.electrical_contract;

import competition.subsystems.pose.PoseSubsystem;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class CompetitionContract extends ElectricalContract {

    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public CompetitionContract() {
    }

    @Override
    public boolean isDriveReady() {
        return true;
    }

    @Override
    public boolean areCanCodersReady() {
        return true;
    }

    private String getDriveControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Drive";
    }

    private String getSteeringControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Steering";
    }

    private String getSteeringEncoderControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/SteeringEncoder";
    }

    @Override
    public DeviceInfo getDriveMotor(SwerveInstance swerveInstance) {
        switch (swerveInstance.label()) {
            case "FrontLeftDrive":
                return new DeviceInfo(getDriveControllerName(swerveInstance), 31, false, simulationScalingValue);

            case "FrontRightDrive":
                return new DeviceInfo(getDriveControllerName(swerveInstance), 29, false, simulationScalingValue);

            case "RearLeftDrive":
                return new DeviceInfo(getDriveControllerName(swerveInstance), 38, false, simulationScalingValue);

            case "RearRightDrive":
                return new DeviceInfo(getDriveControllerName(swerveInstance), 21, false, simulationScalingValue);

            default:
                return null;
        }
    }

    @Override
    public DeviceInfo getSteeringMotor(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        switch (swerveInstance.label()) {
            case "FrontLeftDrive":
                return new DeviceInfo(getSteeringControllerName(swerveInstance), 30, false, simulationScalingValue);

            case "FrontRightDrive":
                return new DeviceInfo(getSteeringControllerName(swerveInstance), 28, false, simulationScalingValue);

            case "RearLeftDrive":
                return new DeviceInfo(getSteeringControllerName(swerveInstance), 39, false, simulationScalingValue);

            case "RearRightDrive":
                return new DeviceInfo(getSteeringControllerName(swerveInstance), 20, false, simulationScalingValue);

            default:
                return null;
        }
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        switch (swerveInstance.label()) {
            case "FrontLeftDrive":
                return new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 51, false, simulationScalingValue);

            case "FrontRightDrive":
                return new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 52, false, simulationScalingValue);

            case "RearLeftDrive":
                return new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 53, false, simulationScalingValue);

            case "RearRightDrive":
                return new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 54, false, simulationScalingValue);

            default:
                return null;
        }
    }

    @Override
    public XYPair getSwerveModuleOffsets(SwerveInstance swerveInstance) {
        switch (swerveInstance.label()) {
            case "FrontLeftDrive":
                return new XYPair(15, 15);
            case "FrontRightDrive":
                return new XYPair(15, -15);
            case "RearLeftDrive":
                return new XYPair(-15, 15);
            case "RearRightDrive":
                return new XYPair(-15, -15);
            default:
                return new XYPair(0, 0);
        }
    }

    public DeviceInfo getCollectorMotor() {
        return new DeviceInfo("CollectorMotor", 25, true);
    }

    @Override
    public boolean isCollectorReady() {
        return true;
    }

    @Override
    public DeviceInfo getLightsDio0() {
        return new DeviceInfo("Lights0", 5);
    }

    @Override
    public DeviceInfo getLightsDio1() {
        return new DeviceInfo("Lights1", 6);
    }

    @Override
    public DeviceInfo getLightsDio2() {
        return new DeviceInfo("Lights2", 7);
    }

    @Override
    public DeviceInfo getLightsDio3() {
        return new DeviceInfo("Lights3", 8); // something on the navX, just out of the way
    }

    @Override
    public DeviceInfo getLightsDio4() {
        return new DeviceInfo("Lights4", 9); // something on the navX, just out of the way
    }

    @Override
    public DeviceInfo getLightsCubeDio() {
        return new DeviceInfo("LightsCube", 4);
    }
  
    @Override
    public DeviceInfo getNoteSensorDio() {
        return new DeviceInfo("NoteSensor", 13);
    }

    // ArmSubsystem
    @Override
    public DeviceInfo getArmMotorLeft() {return new DeviceInfo("ArmMotorLeft", 10, true);}

    @Override
    public DeviceInfo getArmMotorRight() {return new DeviceInfo("ArmMotorRight", 11, true);}
}