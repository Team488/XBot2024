package competition.subsystems.drive;

import competition.BaseCompetitionTest;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointAtSpeakerTest extends BaseCompetitionTest {

    @Test
    public void blueAllianceTest() {
        Pose2d straight = new Pose2d(4.5, 5.5, new Rotation2d(60));
        assertEquals(180, getRotationIntentPointAtSpeaker(straight), 1);

        Pose2d angledAbove = new Pose2d(3.5, 7, new Rotation2d(60));
        assertEquals(203, getRotationIntentPointAtSpeaker(angledAbove), 10);

        Pose2d angledBelow = new Pose2d(3.2, 2.5, new Rotation2d(60));
        assertEquals(130, getRotationIntentPointAtSpeaker(angledBelow), 10);
    }

    @Test
    public void redAllianceTest() {
        Pose2d straight = new Pose2d(13, 5.5, new Rotation2d(60));
        assertEquals(0, getRotationIntentPointAtSpeakerRedSide(straight), 10);

        Pose2d angledAbove = new Pose2d(12, 7, new Rotation2d(60));
        assertEquals(340, getRotationIntentPointAtSpeakerRedSide(angledAbove), 10);

        Pose2d angledBelow = new Pose2d(13, 2.5, new Rotation2d(60));
        assertEquals(37, getRotationIntentPointAtSpeakerRedSide(angledBelow), 10);

    }

    private double getRotationIntentPointAtSpeaker(Pose2d currentPose) {
        Translation2d speakerPosition = PoseSubsystem.convertBlueToRedIfNeeded(PoseSubsystem.SPEAKER_TARGET_FORWARD);
        Translation2d currentXY = new Translation2d(currentPose.getX(), currentPose.getY());

        return currentXY.minus(speakerPosition).getAngle().getDegrees() + 180;
    }

    private double getRotationIntentPointAtSpeakerRedSide(Pose2d currentPose) {
        Translation2d speakerPosition = PoseSubsystem.convertBlueToRed(PoseSubsystem.SPEAKER_TARGET_FORWARD);
        Translation2d currentXY = new Translation2d(currentPose.getX(), currentPose.getY());

        return currentXY.minus(speakerPosition).getAngle().getDegrees() + 180;
    }

}
