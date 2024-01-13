package competition.injection.swerve;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SwerveComponentHolder {
    public final SwerveComponent frontLeft;
    public final SwerveComponent frontRight;
    public final SwerveComponent rearLeft;
    public final SwerveComponent rearRight;

    @Inject
    public SwerveComponentHolder(@FrontLeftDrive SwerveComponent frontLeft, @FrontRightDrive SwerveComponent frontRight,
                                 @RearLeftDrive SwerveComponent rearLeft, @RearRightDrive SwerveComponent rearRight) {
        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.rearLeft = rearLeft;
        this.rearRight = rearRight;
    }
}