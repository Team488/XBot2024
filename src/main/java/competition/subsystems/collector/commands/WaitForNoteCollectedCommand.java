package competition.subsystems.collector.commands;

import com.fasterxml.jackson.databind.ser.Serializers;
import competition.subsystems.arm.commands.WaitForArmToBeAtGoalCommand;
import competition.subsystems.collector.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class WaitForNoteCollectedCommand extends BaseCommand {
    CollectorSubsystem collector;
    @Inject
    public WaitForNoteCollectedCommand(CollectorSubsystem collector){
        this.collector = collector;
    }
    @Override
    public void initialize() {
        //empty
    }

    @Override
    public void execute() {
        //empty
    }

    @Override
    public boolean isFinished() {
//        return collector.getGamePieceInControl();
        return false;
    }
}
