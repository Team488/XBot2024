package competition.subsystems.oracle;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Twist2d;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.SwerveSimpleTrajectoryLogic;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.List;

/**
 * This class manages the swerve drive logic for the robot, taking advice from the
 * DynamicOracle about where to go and how to get there. However, it also has some of its own logic;
 * for example, it changes its routing behavior based on whether it is trying to collect a note
 * or score a note.
 *
 * Similar commands should be set up for other major subsystems, such as the collector/shooter/arm.
 * @author JohnGilb
 */
public class SwerveAccordingToOracleCommand extends BaseCommand {

    private int lastSeenInstructionNumber = -1;

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final HeadingModule headingModule;
    public final SwerveSimpleTrajectoryLogic logic;
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
        logic.setWaypointRouter(oracle.getFieldWithObstacles());
        oracle.requestReevaluation();
        setNewInstruction();
    }

    /**
     * This method is called when the DynamicOracle has a new instruction for us to follow.
     * It needs to achieve the final point, but has some discretion about how to get there.
     * Based on what the Oracle is trying to do (collect a note, score a note), we can further
     * optimize the path.
     */
    private void setNewInstruction() {
        // This causes the robot to "point" along the direction of travel for all but the final leg of its travel.
        // This is useful for passing obstacles, as the robot will present a narrower profile and won't have its
        // corners collide with things.
        var goalPosition = oracle.getTerminatingPoint();

        logic.setPrioritizeRotationIfCloseToGoal(true);

        if (oracle.getHighLevelGoal() == DynamicOracle.HighLevelGoal.CollectNote) {
            // Since we're going to grab a note, point the front end of our robot towards the goal,
            // since our collector is on the front side.
            logic.setDriveBackwards(true);
            logic.setAimAtIntermediateNonFinalLegs(false);
            logic.setAimAtGoalDuringFinalLeg(true);

            if (oracle.targetNote == null) {
                // No note, just driving somewhere where we might find some.
                logic.setEnableSpecialAimTarget(false);
                logic.setEnableSpecialAimDuringFinalLeg(false);
                logic.setAimAtGoalDuringFinalLeg(true);
            } else {
                if (oracle.targetNote.getAvailability() == Availability.AgainstObstacle) {
                    // if a note is against an obstacle, we need to always point at it to avoid rotation thrashing as we
                    // try to approach it
                    logic.setEnableSpecialAimTarget(true);
                    logic.setSpecialAimTarget(oracle.getSpecialAimTarget());
                    logic.setAimAtGoalDuringFinalLeg(false);
                } else {
                    // Note is in the clear, just drive straight at it.
                    logic.setEnableSpecialAimTarget(true);
                    logic.setSpecialAimTarget(oracle.getSpecialAimTarget());
                    logic.setAimAtGoalDuringFinalLeg(false);
                }

                logic.setEnableSpecialAimDuringFinalLeg(false);
                // When approaching the note, make sure to aim straight at the note for the best chance of collection.
            }

        } else {
            // We are doing some kind of score operation.
            // Setup general "follow the path" behavior
            logic.setAimAtIntermediateNonFinalLegs(false);
            logic.setAimAtGoalDuringFinalLeg(false);
            // We want to point the back of our robot towards the goal,
            // since our shooter is on the back side of the robot.
            logic.setDriveBackwards(false);
            // This "special aim" mode instructs the robot to aim at a point that isn't the goal point. For example,
            // we could aim directly at the Speaker aperture regardless of where the robot is, causing the robot to
            // "track" the speaker as it moves towards a nice firing point.
            logic.setEnableSpecialAimTarget(false);
            logic.setEnableSpecialAimDuringFinalLeg(false);
            //logic.setSpecialAimTarget(oracle.getSpecialAimTarget());

            // TODO: split the score in speaker and score in Amp into two independent sections. It's likely
            // they will want different behavior.
        }

        // Now that our swerve logic is configured to satisfaction, give it the goal point and reset it to start
        // the new path.
        lastSeenInstructionNumber = goalPosition.getPoseMessageNumber();
        logic.setKeyPoints(List.of(new XbotSwervePoint(
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

        Twist2d powers = logic.calculatePowers(pose.getCurrentPose2d(), drive.getPositionalPid(), headingModule, drive.getMaxTargetSpeedMetersPerSecond());

        aKitLog.record("Powers", powers);

        drive.fieldOrientedDrive(
                new XYPair(powers.dx, powers.dy),
                powers.dtheta, pose.getCurrentHeading().getDegrees(), false);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
