package competition.commandgroups;

import competition.subsystems.drive.commands.PointAtNoteCommand;
import competition.subsystems.drive.commands.PointAtNoteWithBearingCommand;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.VisionSubsystem;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class PointAtNoteCommandGroup extends SequentialCommandGroup {

    @Inject
    public PointAtNoteCommandGroup(PointAtNoteWithBearingCommand pointAtNoteWithBearingCommand,
                                   PointAtNoteCommand pointAtNoteCommand,
                                   PoseSubsystem pose, VisionSubsystem vision, DynamicOracle oracle) {
        var main = new InstantCommand(() -> {
            boolean centerlineTarget = vision.getCenterCamLargestNoteTarget().isPresent();
            if (centerlineTarget) {
                this.addCommands(pointAtNoteWithBearingCommand);
            } else {
                this.addCommands(pointAtNoteCommand);

                // Unnecessary?
                this.addCommands(pointAtNoteWithBearingCommand);
            }
        });

        this.addCommands(main);
    }
}
