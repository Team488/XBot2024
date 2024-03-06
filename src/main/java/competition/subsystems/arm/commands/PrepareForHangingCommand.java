package competition.subsystems.arm.commands;
import xbot.common.command.BaseCommand;
import competition.subsystems.arm.ArmSubsystem;
import javax.inject.Inject;
public class PrepareForHangingCommand extends SetArmExtensionCommand{

    ArmSubsystem armSubsystem;

    @Inject
    public PrepareForHangingCommand(ArmSubsystem armSubsystem){
        super(armSubsystem);
    }
    @Override
    public void initialize(){
        log.info("Preparing Hanging");
        armSubsystem.setTargetValue(armSubsystem.getUsefulArmPositionExtensionInMm(ArmSubsystem.UsefulArmPosition.HANGING_POSITION));
    }


    @Override
    public void execute(){
        //don't think anything is needed
    }
}


