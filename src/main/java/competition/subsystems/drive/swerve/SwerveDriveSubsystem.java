package competition.subsystems.drive.swerve;

import javax.inject.Inject;

import com.revrobotics.CANSparkMax;
//import com.revrobotics.CANSparkMax.FaultID;
//import com.revrobotics.CANSparkMaxLowLevel.PeriodicFrame;

import com.revrobotics.REVLibError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import competition.electrical_contract.ElectricalContract;
import competition.injection.swerve.SwerveInstance;
import competition.injection.swerve.SwerveSingleton;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANSparkMax;
import xbot.common.controls.actuators.XCANSparkMax.XCANSparkMaxFactory;
import xbot.common.math.PIDManager;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

@SwerveSingleton
public class SwerveDriveSubsystem extends BaseSetpointSubsystem<Double> {
    private static Logger log = LogManager.getLogger(SwerveDriveSubsystem.class);

    private final String label;
    private final PIDManager pid;
    private final ElectricalContract contract;
    private final SwerveDriveMotorPidSubsystem pidConfigSubsystem;

    private final DoubleProperty inchesPerMotorRotation;
    private final DoubleProperty targetVelocity;
    private final DoubleProperty currentVelocity;

    private XCANSparkMax motorController;

    @Inject
    public SwerveDriveSubsystem(SwerveInstance swerveInstance, XCANSparkMaxFactory sparkMaxFactory,
                                PropertyFactory pf, PIDManagerFactory pidf, ElectricalContract electricalContract,
                                SwerveDriveMotorPidSubsystem pidConfigSubsystem) {
        this.label = swerveInstance.getLabel();
        log.info("Creating SwerveDriveSubsystem " + this.label);

        // Create properties shared among all instances
        pf.setPrefix(super.getPrefix());
        this.contract = electricalContract;
        this.pid = pidf.create(super.getPrefix() + "PID", 1.0, 0.0, 0.0, -1.0, 1.0);
        this.inchesPerMotorRotation = pf.createPersistentProperty("InchesPerMotorRotation", 2.02249);

        // Create properties unique to this instance.
        pf.setPrefix(this);
        this.targetVelocity = pf.createEphemeralProperty("TargetVelocity", 0.0);
        this.currentVelocity = pf.createEphemeralProperty("CurrentVelocity", 0.0);

        this.pidConfigSubsystem = pidConfigSubsystem;

        if (electricalContract.isDriveReady()) {
            this.motorController = sparkMaxFactory.createWithoutProperties(electricalContract.getDriveNeo(swerveInstance), this.getPrefix(), "DriveNeo");
            setMotorControllerPositionPidParameters();
//            setupStatusFrames();
            this.motorController.setSmartCurrentLimit(45);
            this.motorController.setIdleMode(CANSparkMax.IdleMode.kBrake);
        }
    }

    /**
     * Set up status frame intervals to reduce unnecessary CAN activity.
     */
//    private void setupStatusFrames() {
//        if (this.contract.isDriveReady()) {
//            // We need to re-set frame intervals after a device reset.
////            if (this.motorController.getStickyFault(FaultID.kHasReset) && this.motorController.getLastError() != REVLibError.kHALError) {
//                log.info("Setting status frame periods.");
//
//                // See https://docs.revrobotics.com/sparkmax/operating-modes/control-interfaces#periodic-status-frames
//                // for description of the different status frames. kStatus2 is the only frame with data needed for software PID.
//
////                this.motorController.getInternalSparkMax().setPeriodicFramePeriod(PeriodicFrame.kStatus0, 500 /* default 10 */);
////                this.motorController.getInternalSparkMax().setPeriodicFramePeriod(PeriodicFrame.kStatus1, 20 /* default 20 */);
////                this.motorController.getInternalSparkMax().setPeriodicFramePeriod(PeriodicFrame.kStatus2, 20 /* default 20 */);
////                this.motorController.getInternalSparkMax().setPeriodicFramePeriod(PeriodicFrame.kStatus3, 500 /* default 50 */);
//
//                this.motorController.clearFaults();
//            }
//        }
//    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String getPrefix() {
        return super.getPrefix() + this.label + "/";
    }

    /**
     * Gets current velocity in inches per second
     */
    @Override
    public Double getCurrentValue() {
        if (this.contract.isDriveReady()) {
            // Spark returns in RPM - need to convert to inches per second
            return this.motorController.getVelocity() * this.inchesPerMotorRotation.get() / 60.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Gets target velocity in inches per second
     */
    @Override
    public Double getTargetValue() {
        return this.targetVelocity.get();
    }

    /**
     * Sets target velocity in inches per second
     */
    @Override
    public void setTargetValue(Double value) {
        this.targetVelocity.set(value);
    }

    public double getCurrentPositionValue() {
        if (this.contract.isDriveReady()) {
            return this.motorController.getPosition() * this.inchesPerMotorRotation.get();
        } else {
            return 0;
        }
    }

    @Override
    public void setPower(Double power) {
        if (this.contract.isDriveReady()) {
            this.motorController.set(power);
        }
    }

    @Override
    public boolean isCalibrated() {
        return true;
    }


    public XCANSparkMax getSparkMax() {
        return this.motorController;
    }

    public void resetPid() {
        this.pid.reset();
    }

    public double calculatePower() {
        return this.pid.calculate(this.getTargetValue(), this.getCurrentValue());
    }

    public void setMotorControllerPositionPidParameters() {
        if (this.contract.isDriveReady()) {
            this.motorController.setP(pidConfigSubsystem.getP());
            this.motorController.setI(pidConfigSubsystem.getI());
            this.motorController.setD(pidConfigSubsystem.getD());
            this.motorController.setFF(pidConfigSubsystem.getFF());
            this.motorController.setOutputRange(pidConfigSubsystem.getMinOutput(), pidConfigSubsystem.getMaxOutput());
            this.motorController.setClosedLoopRampRate(pidConfigSubsystem.getClosedLoopRampRate());
            this.motorController.setOpenLoopRampRate(pidConfigSubsystem.getOpenLoopRampRate());
        }
    }

    @Override
    public void periodic() {
        if (contract.isDriveReady()) {
            currentVelocity.set(this.getCurrentValue());
//            setupStatusFrames();
            this.motorController.periodic();
        }
    }
}