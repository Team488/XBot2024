package competition.subsystems.drive;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.controls.actuators.XCANTalon;
import xbot.common.controls.actuators.XCANTalon.XCANTalonFactory;
import xbot.common.math.PIDManager;
import xbot.common.math.XYPair;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.properties.XPropertyManager;
import xbot.common.subsystems.drive.BaseDriveSubsystem;

@Singleton
public class DriveSubsystem extends BaseDriveSubsystem {
    private static Logger log = LogManager.getLogger(DriveSubsystem.class);
    
    ElectricalContract contract;
    
    public final XCANTalon leftLeader;
    public final XCANTalon rightLeader;

    private final PIDManager positionPid;
    private final PIDManager rotationPid;

    private double scalingFactorFromTicksToInches = 1.0 / 256.0;

    @Inject
    public DriveSubsystem(XCANTalonFactory talonFactory, XPropertyManager propManager, ElectricalContract contract, PIDManagerFactory pf) {
        log.info("Creating DriveSubsystem");

        this.leftLeader = talonFactory.create(contract.getLeftLeader());
        this.rightLeader = talonFactory.create(contract.getRightLeader());

        positionPid = pf.create(getPrefix() + "PositionPID");
        rotationPid = pf.create(getPrefix() + "RotationPID");
    }

    public void tankDrive(double leftPower, double rightPower) {
        this.leftLeader.simpleSet(leftPower);
        this.rightLeader.simpleSet(rightPower);
    }

    @Override
    public PIDManager getPositionalPid() {
        return positionPid;
    }

    @Override
    public PIDManager getRotateToHeadingPid() {
        return rotationPid;
    }

    @Override
    public PIDManager getRotateDecayPid() {
        return null;
    }

    @Override
    public void move(XYPair translate, double rotate) {
        double y = translate.y;

        double left = y - rotate;
        double right = y + rotate;

        this.leftLeader.simpleSet(left);
        this.rightLeader.simpleSet(right);
    }

    @Override
    public double getLeftTotalDistance() {
        return leftLeader.getSelectedSensorPosition(0) * scalingFactorFromTicksToInches;
    }

    @Override
    public double getRightTotalDistance() {
        return rightLeader.getSelectedSensorPosition(0) * scalingFactorFromTicksToInches;
    }

    @Override
    public double getTransverseDistance() {
        return 0;
    }
}
