package competition.subsystems.arm.commands;
import xbot.common.command.BaseCommand;
import competition.subsystems.arm.ArmSubsystem;
import javax.inject.Inject;
public class PrepareForHangingCommand extends BaseSetpointCommand{

    ArmSubsystem armSubsystem;
    private double TargetExtension;

    @Inject
    public PrepareForHangingCommand(ArmSubsystem armSubsystem){
        super(armSubsystem);
        this.armSubsystem = armSubsystem;
    }
    public void setTargetExtension(double TargetExtension){
        this.targetExtension = HANGING_POSITION
    }
    @Override
    public void initialize(){
        log.info("Preparing Hanging");
    }

    @Override
    public void execute(){
    }
}


