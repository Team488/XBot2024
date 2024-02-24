package competition.subsystems.drive;

import edu.wpi.first.math.geometry.Pose2d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.injection.swerve.FrontLeftDrive;
import xbot.common.injection.swerve.FrontRightDrive;
import xbot.common.injection.swerve.RearLeftDrive;
import xbot.common.injection.swerve.RearRightDrive;
import xbot.common.injection.swerve.SwerveComponent;
import xbot.common.math.PIDDefaults;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DriveSubsystem extends BaseSwerveDriveSubsystem implements DataFrameRefreshable {
    private static final Logger log = LogManager.getLogger(DriveSubsystem.class);

    private Pose2d targetNote;

    @Inject
    public DriveSubsystem(PIDManagerFactory pidFactory, PropertyFactory pf,
                          @FrontLeftDrive SwerveComponent frontLeftSwerve, @FrontRightDrive SwerveComponent frontRightSwerve,
                          @RearLeftDrive SwerveComponent rearLeftSwerve, @RearRightDrive SwerveComponent rearRightSwerve) {
        super(pidFactory, pf, frontLeftSwerve, frontRightSwerve, rearLeftSwerve, rearRightSwerve);
        log.info("Creating DriveSubsystem");
    }

    @Override
    protected PIDDefaults getPositionalPIDDefaults() {
        return super.getPositionalPIDDefaults();
    }

    @Override
    protected PIDDefaults getHeadingPIDDefaults() {
        return super.getHeadingPIDDefaults();
    }

    public void setTargetNote(Pose2d targetNote) {
        this.targetNote = targetNote;
    }

    public Pose2d getTargetNote() {
        return targetNote;
    }

}