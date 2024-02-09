package competition.electrical_contract;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.injection.electrical_contract.CANTalonInfo;
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

    public DeviceInfo getCollectorMotor() {
        return new DeviceInfo("CollectorMotor", 25, true);
    }

    @Override
    public DeviceInfo getShooterMotorLeader() {
        return new DeviceInfo("ShooterLeader", 50, false);
    }

    @Override
    public DeviceInfo getShooterMotorFollower() {
        return new DeviceInfo("ShooterFollower", 49, false);
    }

    @Override
    public boolean isCollectorReady() {
        return true;
    }

    public boolean isScoocherReady() {
        return false;
    }
    public DeviceInfo getScoocherMotor(){
        return new DeviceInfo("ScoocherMotor", 14);
    }

    public DeviceInfo getCollectorSolenoid() {
        return new DeviceInfo("CollectorSolenoid", 2, false);
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
    public DeviceInfo getInControlNoteSensorDio() {
        return new DeviceInfo("InControlNoteSensor", 13);
    }
    @Override
    public DeviceInfo getReadyToFireNoteSensorDio() {
        return new DeviceInfo("ReadyToFireNoteSensor", 15);
    }

    // ArmSubsystem

    @Override
    public boolean isArmReady() {
        return false;
    }

    @Override
    public DeviceInfo getArmMotorLeft() {
        return new DeviceInfo("ArmMotorLeft", 10, true);
    }

    @Override
    public DeviceInfo getArmMotorRight() {
        return new DeviceInfo("ArmMotorRight", 11, true);
    }
    @Override
    public DeviceInfo getBrakeSolenoid(){return new DeviceInfo("BrakeSolenoid", 1, false);}
}



