package competition.subsystems.oracle;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import org.apache.logging.log4j.LogManager;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ManualRobotKnowledgeSubsystem extends BaseSubsystem implements NoteCollectionInfoSource, NoteFiringInfoSource {

    private boolean noteCollected;
    private boolean noteShot;

    @Inject
    public ManualRobotKnowledgeSubsystem(){}

    @Override
    public boolean confidentlyHasControlOfNote() {
        return noteCollected;
    }

    @Override
    public boolean confidentlyHasFiredNote() {
        return noteShot;
    }

    public Command createSetNoteCollectedCommand() {
        return Commands.runEnd(() -> noteCollected = true, ()-> noteCollected = false);
    }

    public Command createSetNoteShotCommand() {
        return Commands.runEnd(() -> noteShot = true, ()-> noteShot = false);
    }

    @Override
    public void periodic() {
        Logger.recordOutput(getPrefix()+"NoteCollected", noteCollected);
        Logger.recordOutput(getPrefix()+"NoteShot", noteShot);
    }
}
