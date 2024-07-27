package competition.electrical_contract;

import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.math.XYPair;


import javax.inject.Inject;

public class PracticeContract extends CompetitionContract {
    @Inject
    public PracticeContract() {

    }

    public boolean isScoocherReady() {
        return false;
    }

    @Override
    public boolean isArmReady() {
        return false;
    }

    @Override
    public boolean isShooterReady() {
        return false;
    }

    @Override
    public boolean isDriveReady() {
        return true;
    }

    @Override
    public boolean areCanCodersReady() {
        return true;
    }


    @Override
    public DeviceInfo getDriveMotor(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 31, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 29, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 38, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 21, false, simulationScalingValue);
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringMotor(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 30, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 28, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 39, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 20, false, simulationScalingValue);
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 51, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 52, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 53, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 54, false, simulationScalingValue);
            default -> null;
        };
    }

    @Override
    public XYPair getSwerveModuleOffsets(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" -> new XYPair(15, 15);
            case "FrontRightDrive" -> new XYPair(15, -15);
            case "RearLeftDrive" -> new XYPair(-15, 15);
            case "RearRightDrive" -> new XYPair(-15, -15);
            default -> new XYPair(0, 0);
        };

    }
}


