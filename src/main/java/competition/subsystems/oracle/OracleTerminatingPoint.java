package competition.subsystems.oracle;

import edu.wpi.first.math.geometry.Pose2d;

public class OracleTerminatingPoint  {
    private Pose2d terminatingPose;
    private int poseMessageCount;

    public OracleTerminatingPoint(Pose2d terminatingPose, int poseMessageCount) {
        this.terminatingPose = terminatingPose;
        this.poseMessageCount = poseMessageCount;
    }

    public Pose2d getTerminatingPose() {
        return terminatingPose;
    }

    public int getPoseMessageNumber() {
        return poseMessageCount;
    }
}
