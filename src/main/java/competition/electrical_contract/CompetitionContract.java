package competition.electrical_contract;

import javax.inject.Inject;

import competition.injection.swerve.SwerveInstance;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.math.XYPair;

public class CompetitionContract extends ElectricalContract {

    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public CompetitionContract() {}

    @Override
    public boolean isDriveReady() {
        return true;
    }

    @Override
    public boolean areCanCodersReady() {
        return true;
    }

    @Override
    public DeviceInfo getDriveNeo(SwerveInstance swerveInstance) {
        switch (swerveInstance.getLabel()) {
            case "FrontLeftDrive":
                return new DeviceInfo(31, false, simulationScalingValue);

            case "FrontRightDrive":
                return new DeviceInfo(29, false, simulationScalingValue);

            case "RearLeftDrive":
                return new DeviceInfo(38, false, simulationScalingValue);

            case "RearRightDrive":
                return new DeviceInfo(21, false, simulationScalingValue);

            default:
                return null;
        }
    }

    @Override
    public DeviceInfo getSteeringNeo(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        switch (swerveInstance.getLabel()) {
            case "FrontLeftDrive":
                return new DeviceInfo(30, false, simulationScalingValue);

            case "FrontRightDrive":
                return new DeviceInfo(28, false, simulationScalingValue);

            case "RearLeftDrive":
                return new DeviceInfo(39, false, simulationScalingValue);

            case "RearRightDrive":
                return new DeviceInfo(20, false, simulationScalingValue);

            default:
                return null;
        }
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        switch (swerveInstance.getLabel()) {
            case "FrontLeftDrive":
                return new DeviceInfo(51, false, simulationScalingValue);

            case "FrontRightDrive":
                return new DeviceInfo(52, false, simulationScalingValue);

            case "RearLeftDrive":
                return new DeviceInfo(53, false, simulationScalingValue);

            case "RearRightDrive":
                return new DeviceInfo(54, false, simulationScalingValue);

            default:
                return null;
        }
    }

    @Override
    public XYPair getSwerveModuleOffsets(SwerveInstance swerveInstance) {
        switch (swerveInstance.getLabel()) {
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

    @Override
    public DeviceInfo getLowerArmLeftMotor() {
        return new DeviceInfo(37,true);
    }

    @Override
    public DeviceInfo getLowerArmRightMotor() {
        return new DeviceInfo(22,false);
    }

    public DeviceInfo getUpperArmLeftMotor(){
        return new DeviceInfo(35,true);
    }

    @Override
    public DeviceInfo getUpperArmRightMotor() {
        return new DeviceInfo(24,true);
    }

    public boolean isLowerArmReady() { return true;}

    public boolean isUpperArmReady() { return true;}

    @Override
    public boolean isLowerArmEncoderReady() {
        return true;
    }

    @Override
    public boolean isUpperArmEncoderReady() {
        return true;
    }

    @Override
    public DeviceInfo getLowerArmEncoder() {
        return new DeviceInfo(0, true);
    }

    @Override
    public DeviceInfo getUpperArmEncoder() {
        return new DeviceInfo(1, true);
    }

    public DeviceInfo getClawSolenoid() {return new DeviceInfo(0, false);}

    @Override
    public DeviceInfo getLeftClawMotor() {
        return new DeviceInfo(34, true, 1);
    }

    @Override
    public DeviceInfo getRightClawMotor() {
        return new DeviceInfo(33, false, 1);
    }

    @Override
    public boolean areClawMotorsReady() {
        return true;
    }

    @Override
    public DeviceInfo getLowerArmBrakeSolenoid() {
        return new DeviceInfo(1, false);
    }

    public DeviceInfo getCollectorMotor(){ return new DeviceInfo(25,true);}

    @Override
    public boolean isCollectorReady() { return true; }

    public DeviceInfo getCollectorSolenoid(){ return new DeviceInfo(2,false);}

    @Override
    public DeviceInfo getLightsDio0() {
        return new DeviceInfo(5);
    }

    @Override
    public DeviceInfo getLightsDio1() {
        return new DeviceInfo(6);
    }

    @Override
    public DeviceInfo getLightsDio2() {
        return new DeviceInfo(7);
    }

    @Override
    public DeviceInfo getLightsDio3() {
        return new DeviceInfo(8); // something on the navX, just out of the way
    }

    @Override
    public DeviceInfo getLightsDio4() {
        return new DeviceInfo(9); // something on the navX, just out of the way
    }

    @Override
    public DeviceInfo getLightsCubeDio() {
        return new DeviceInfo(4);
    }

    public DeviceInfo getPressureSensor() {return new DeviceInfo(3);}
}