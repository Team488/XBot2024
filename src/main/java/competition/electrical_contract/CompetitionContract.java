package competition.electrical_contract;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.math.XYPair;
import xbot.common.subsystems.vision.CameraCapabilities;

import javax.inject.Inject;
import java.util.EnumSet;

public class CompetitionContract extends ElectricalContract {
    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public CompetitionContract() {
    }

    @Override
    public boolean isShooterReady() {
        return true;
    }

    @Override
    public boolean isDriveReady() {
        return true;
    }

    @Override
    public boolean areCanCodersReady() {
        return true;
    }

    protected String getDriveControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Drive";
    }

    protected String getSteeringControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Steering";
    }

    protected String getSteeringEncoderControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/SteeringEncoder";
    }

    @Override
    public DeviceInfo getDriveMotor(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 39, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 31, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 20, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getDriveControllerName(swerveInstance), 29, false, simulationScalingValue);
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringMotor(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 38, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 30, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 21, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringControllerName(swerveInstance), 28, false, simulationScalingValue);
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 54, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 53, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 52, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 51, false, simulationScalingValue);
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
        return new DeviceInfo("CollectorMotor", 37, false);
    }

    @Override
    public DeviceInfo getShooterMotorLeader() {
        return new DeviceInfo("ShooterLeader", 23, false);
    }

    @Override
    public DeviceInfo getShooterMotorFollower() {
        return new DeviceInfo("ShooterFollower", 36, false);
    }

    @Override
    public boolean isCollectorReady() {
        return true;
    }

    public boolean isScoocherReady() {
        return true;
    }
    public DeviceInfo getScoocherMotor(){
        return new DeviceInfo("ScoocherMotor", 33);
    }

    public DeviceInfo getCollectorSolenoid() {
        return new DeviceInfo("CollectorSolenoid", 2, false);
    }

    @Override
    public DeviceInfo getLightsDio0() {
        return new DeviceInfo("Lights0", 0);
    }

    @Override
    public DeviceInfo getLightsDio1() {
        return new DeviceInfo("Lights1", 1);
    }

    @Override
    public DeviceInfo getLightsDio2() {
        return new DeviceInfo("Lights2", 2);
    }

    @Override
    public DeviceInfo getLightsDio3() {
        return new DeviceInfo("Lights3", 3);
    }

    @Override
    public DeviceInfo getFlipperServo() {
        return new DeviceInfo("FlipperLeft", 5);
    }

    @Override
    public DeviceInfo getInControlNoteSensorDio() {
        return new DeviceInfo("InControlNoteSensor", 8, true);
    }
    @Override
    public DeviceInfo getReadyToFireNoteSensorDio() {
        return new DeviceInfo("ReadyToFireNoteSensor", 9, true);
    }

    // ArmSubsystem

    @Override
    public boolean isArmReady() {
        return true;
    }

    @Override
    public DeviceInfo getArmMotorLeft() {
        return new DeviceInfo("ArmMotorLeft", 32, true);
    }

    @Override
    public DeviceInfo getArmMotorRight() {
        return new DeviceInfo("ArmMotorRight", 27, true);
    }

    @Override
    public DeviceInfo getBrakeSolenoidForward(){return new DeviceInfo("ForwardBrake", 14, false);}
    public DeviceInfo getBrakeSolenoidReverse(){return new DeviceInfo("ReverseBrake", 15, false);}

    @Override
    public boolean getArmEncoderInverted() {
        return false;
    }

    @Override
    public boolean getArmEncoderIsOnLeftMotor() {
        return true;
    }

    private static double aprilCameraXDisplacement = 13.153 / PoseSubsystem.INCHES_IN_A_METER;
    private static double aprilCameraYDisplacement = 12.972 / PoseSubsystem.INCHES_IN_A_METER;
    private static double aprilCameraZDisplacement = 9.014 / PoseSubsystem.INCHES_IN_A_METER;
    private static double aprilCameraPitch = Math.toRadians(-55.5);
    private static double aprilCameraYaw = Math.toRadians(10);

    public static String rearLeftNoteCameraName = "NoteRearLeft";
    public static String rearRightNoteCameraName = "NoteRearRight";
    public static String rearCenterNoteCameraName= "NoteRearCenter"; //TODO - one of these cameras

    @Override
    public CameraInfo[] getCameraInfo() {
        return new CameraInfo[] {
            new CameraInfo("Apriltag_FrontLeft_Camera",
                    "AprilTagFrontLeft",
                    new Transform3d(new Translation3d(
                            aprilCameraXDisplacement,
                            aprilCameraYDisplacement,
                            aprilCameraZDisplacement),
                            new Rotation3d(0, aprilCameraPitch, aprilCameraYaw)),
                    EnumSet.of(CameraCapabilities.APRIL_TAG)),
            new CameraInfo("Apriltag_FrontRight_Camera",
                    "AprilTagFrontRight",
                    new Transform3d(new Translation3d(
                            aprilCameraXDisplacement,
                            -aprilCameraYDisplacement,
                            aprilCameraZDisplacement),
                            new Rotation3d(0, aprilCameraPitch, -aprilCameraYaw)),
                    EnumSet.of(CameraCapabilities.APRIL_TAG)),
            new CameraInfo("Apriltag_RearLeft_Camera",
                    "AprilTagRearLeft",
                    new Transform3d(new Translation3d(
                            -aprilCameraXDisplacement,
                            aprilCameraYDisplacement,
                            aprilCameraZDisplacement),
                            new Rotation3d(0, aprilCameraPitch, Math.toRadians(180) - aprilCameraYaw)),
                    EnumSet.of(CameraCapabilities.APRIL_TAG)),
            new CameraInfo("Apriltag_RearRight_Camera",
                    "AprilTagRearRight",
                    new Transform3d(new Translation3d(
                            -aprilCameraXDisplacement,
                            -aprilCameraYDisplacement,
                            aprilCameraZDisplacement),
                            new Rotation3d(0, aprilCameraPitch, Math.toRadians(180) + aprilCameraYaw)),
                    EnumSet.of(CameraCapabilities.APRIL_TAG)),
            new CameraInfo("GamePiece_FrontLeft_Camera",
                    rearCenterNoteCameraName,
                    new Transform3d(new Translation3d(), new Rotation3d()),
                    EnumSet.of(CameraCapabilities.GAME_SPECIFIC)),
            new CameraInfo("GamePiece_FrontRight_Camera",
                    "NoteFrontRight",
                    new Transform3d(new Translation3d(), new Rotation3d()),
                    EnumSet.of(CameraCapabilities.GAME_SPECIFIC)),
            new CameraInfo("GamePiece_RearLeft_Camera",
                    rearLeftNoteCameraName,
                new Transform3d(new Translation3d(), new Rotation3d()),
                    EnumSet.of(CameraCapabilities.GAME_SPECIFIC)),
            new CameraInfo("GamePiece_RearRight_Camera",
                    rearRightNoteCameraName,
                    new Transform3d(new Translation3d(), new Rotation3d()),
                    EnumSet.of(CameraCapabilities.GAME_SPECIFIC))
        };
    }
}



