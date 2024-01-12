package competition.subsystems.drive.swerve;

import javax.inject.Inject;

import competition.subsystems.BaseMotorPidSubsystem;
import xbot.common.properties.PropertyFactory;

/**
 * Container for drive motor controller PIDs.
 */
public class SwerveDriveMotorPidSubsystem extends BaseMotorPidSubsystem {

    @Inject
    public SwerveDriveMotorPidSubsystem(PropertyFactory pf) {
        super(pf);
    }

}