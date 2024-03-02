package competition.subsystems.arm.commands;
import xbot.common.command.BaseCommand;
import competition.subsystems.arm.ArmSubsystem;
import javax.inject.Inject;
public class PrepareForHangingCommand extends BaseCommand{

    ArmSubsystem armSubsystem;

    @Inject
    public PrepareForHangingCommand(ArmSubsystem armSubsystem){
        this.armSubsystem = armSubsystem;
    }
    @Override
    public void initialize(){
        log.info("Preparing Hanging");
        armSubsystem.setTargetAngle(armSubsystem.getUsefulArmPositionAngle(ArmSubsystem.UsefulArmPosition.HANGING_POSITION));
    }


    @Override
    public void execute(){
        //don't think anything is needed
    }
}


