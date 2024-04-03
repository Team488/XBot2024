package competition.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Twist2d;

import java.util.Optional;

public class NoteSeekAdvice {
    public Optional<Pose2d> suggestedPose;
    public NoteAcquisitionMode noteAcquisitionMode;
    public Optional<Twist2d> suggestedDrivePercentages;

    public NoteSeekAdvice(NoteAcquisitionMode noteAcquisitionMode, Optional<Pose2d> suggestedPose,
                          Optional<Twist2d> suggestedDrivePercentages) {
        this.suggestedPose = suggestedPose;
        this.noteAcquisitionMode = noteAcquisitionMode;
        this.suggestedDrivePercentages = suggestedDrivePercentages;
    }
}
