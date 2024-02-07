package competition.auto_programs;

import competition.subsystems.arm.commands.SetArmAngleCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class BasicAutonomous extends SequentialCommandGroup {

    @Inject
    BasicAutonomous(SetArmAngleCommand setArmAngleCommand) {
        setArmAngleCommand.setTargetAngle();
    }
}
