package competition.subsystems.vision;

public enum NoteAcquisitionMode {
    BlindApproach,
    RotateToNoteDetectedByCornerCameras,
    CenterCameraVisionApproach,
    CenterCameraTerminalApproach,
    BackAwayToTryAgain,
    SearchViaRotation,
    GiveUp
}
