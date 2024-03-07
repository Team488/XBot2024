package competition.subsystems.arm.commands;
import competition.subsystems.arm.ArmSubsystem;
import javax.inject.Inject;
public class PrepareForHangingCommand extends SetArmExtensionCommand{
    @Inject
    public PrepareForHangingCommand(ArmSubsystem armSubsystem){
        super(armSubsystem);
        this.setTargetExtension(ArmSubsystem.UsefulArmPosition.HANG_APPROACH);
    }
}


