package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.subsystems.pose.BasePoseSubsystem;

public enum PointOfInterest {

    // Notes
    SpikeTop(false, PoseSubsystem.BlueSpikeTop),
    SpikeMiddle(false, PoseSubsystem.BlueSpikeMiddle),
    SpikeBottom(false, PoseSubsystem.BlueSpikeBottom),
    UnderChain(false, PoseSubsystem.BlueUnderTheChain),
    CenterLine1(true, PoseSubsystem.CenterLine1),
    CenterLine2(true, PoseSubsystem.CenterLine2),
    CenterLine3(true, PoseSubsystem.CenterLine3),
    CenterLine4(true, PoseSubsystem.CenterLine4),
    CenterLine5(true, PoseSubsystem.CenterLine5),

    // Scoring locations
    AmpScoringLocation(false, PoseSubsystem.BlueAmpScoringLocation),
    SubwooferTopScoringLocation(false, PoseSubsystem.BlueSubwooferTopScoringLocation),
    SubwooferMiddleScoringLocation(false, PoseSubsystem.BlueSubwooferMiddleScoringLocation),
    SubwooferBottomScoringLocation(false, PoseSubsystem.BlueSubwooferBottomScoringLocation),
    PodiumScoringLocation(false, PoseSubsystem.BluePodiumScoringLocation),
    AmpFarScoringLocation(false, PoseSubsystem.BlueFarAmpScoringLocation),
    BottomSpikeCloserToSpeakerScoringLocation(false, PoseSubsystem.BlueBottomSpikeCloserToSpeakerScoringLocation),
    MiddleSpikeScoringLocation(false, PoseSubsystem.BlueMiddleSpikeScoringLocation),
    TopSpikeCloserToSpeakerScoringLocation(false, PoseSubsystem.BlueTopSpikeCloserToSpeakerScoringLocation),
    OneRobotAwayFromCenterSubwooferScoringLocation(false, PoseSubsystem.BlueOneRobotAwayFromCenterSubwooferScoringLocation),

    // Navigational aids
    SpikeTopWhiteLine(false, PoseSubsystem.BlueSpikeTopWhiteLine),
    SpikeBottomWhiteLine(false, PoseSubsystem.BlueSpikeBottomWhiteLine),
    PodiumWaypoint(false, PoseSubsystem.BluePodiumWaypoint),
    TopWingUpper(false, PoseSubsystem.BlueTopWingUpper),
    TopWingLower(false, PoseSubsystem.BlueTopWingLower),
    BottomWing(false, PoseSubsystem.BlueBottomWing),

    // Stage Navigation
    StageNW(false, PoseSubsystem.BlueStageNW),
    StageE(false, PoseSubsystem.BlueStageE),
    StageSW(false, PoseSubsystem.BlueStageSW),
    StageCenter(false, PoseSubsystem.BlueStageCenter),
    SouthOfStage(false, PoseSubsystem.BlueSouthOfStage),

    // Source Slots
    SourceNearest(false, PoseSubsystem.BlueSourceNearest),
    SourceMiddle(false, PoseSubsystem.BlueSourceMiddle),
    SourceFarthest(false, PoseSubsystem.BlueSourceFarthest),

    // Hanging
    StageNWHangingPreparationPoint(false, PoseSubsystem.BlueStageNWHangingPreparationPoint),
    StageEHangingPreparationPoint(false, PoseSubsystem.BlueStageEHangingPreparationPoint),
    StageSWHangingPreparationPoint(false, PoseSubsystem.BlueStageSWHangingPreparationPoint);

    private boolean unique;
    private Pose2d location;

    PointOfInterest(boolean unique, Pose2d blueOrUniqueLocation) {
        this.unique = unique;
        this.location = blueOrUniqueLocation;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getBlueName() {
        return "Blue" + name();
    }

    public String getRedName() {
        return "Red" + name();
    }

    public String getName(DriverStation.Alliance alliance) {
        if (isUnique()) {
            return name();
        }
        if (alliance == DriverStation.Alliance.Red) {
            return getRedName();
        } else {
            return getBlueName();
        }
    }

    public String getName() {
        if (isUnique()) {
            return name();
        }
        if (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red) {
            return getRedName();
        } else {
            return getBlueName();
        }
    }

    public Pose2d getBlueLocation() {
        return location;
    }

    public Pose2d getRedLocation() {
        return BasePoseSubsystem.convertBluetoRed(location);
    }

    public Pose2d getLocation() {
        if (isUnique()) {
            return location;
        }
        if (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red) {
            return getRedLocation();
        } else {
            return getBlueLocation();
        }
    }

    public Pose2d getLocation(DriverStation.Alliance alliance) {
        if (alliance == DriverStation.Alliance.Red) {
            return getRedLocation();
        } else {
            return getBlueLocation();
        }
    }

}
