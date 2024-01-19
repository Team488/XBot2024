package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.oracle.DynamicOracle;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Twist2d;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.SwerveSimpleTrajectoryLogic;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.List;

public class SwerveAccordingToOracleCommand extends BaseCommand {

    private int lastSeenInstructionNumber = -1;

    DriveSubsystem drive;
    PoseSubsystem pose;
    HeadingModule headingModule;
    public SwerveSimpleTrajectoryLogic logic;
    DynamicOracle oracle;

    @Inject
    public SwerveAccordingToOracleCommand(DriveSubsystem drive, PoseSubsystem pose,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          DynamicOracle oracle) {
        this.drive = drive;
        this.pose = pose;
        headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.oracle = oracle;

        pf.setPrefix(this);
        this.addRequirements(drive);
        logic = new SwerveSimpleTrajectoryLogic();
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        oracle.requestReevaluation();
        setNewInstruction();
    }

    private void setNewInstruction() {
        logic.setAimAtIntermediateNonFinalLegs(true);

        var goalPosition = oracle.getTerminatingPoint();
        if (oracle.getHighLevelGoal() == DynamicOracle.HighLevelGoal.CollectNote) {
            logic.setDriveBackwards(false);
            logic.setAimAtGoalDuringFinalLeg(true);
            logic.setEnableSpecialAimTarget(false);
        } else {
            logic.setAimAtGoalDuringFinalLeg(true);
            logic.setDriveBackwards(true);
            logic.setEnableSpecialAimTarget(true);
            logic.setSpecialAimTarget(oracle.getSpecialAimTarget());
        }

        lastSeenInstructionNumber = goalPosition.getPoseMessageNumber();
        logic.setKeyPoints(List.of(XbotSwervePoint.createXbotSwervePoint(
                goalPosition.getTerminatingPose().getTranslation(),
                goalPosition.getTerminatingPose().getRotation(),
                10)));

        logic.reset(pose.getCurrentPose2d());
    }

    @Override
    public void execute() {

        if (oracle.getTerminatingPoint().getPoseMessageNumber() != lastSeenInstructionNumber) {
            setNewInstruction();
        }

        Twist2d powers = logic.calculatePowers(pose.getCurrentPose2d(), drive.getPositionalPid(), headingModule);

        Logger.recordOutput(getPrefix()+"Powers", powers);

        drive.fieldOrientedDrive(
                new XYPair(powers.dx, powers.dy),
                powers.dtheta, pose.getCurrentHeading().getDegrees(), false);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
