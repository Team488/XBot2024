package competition.subsystems.drive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import competition.BaseCompetitionTest;

public class DriveSubsystemTest extends BaseCompetitionTest {

    @Test
    public void testSwerveModuleInjection(){
        DriveSubsystem driveSubsystem = (DriveSubsystem)getInjectorComponent().driveSubsystem();

        assertNotSame(
                driveSubsystem.getFrontLeftSwerveModuleSubsystem(),
                driveSubsystem.getFrontRightSwerveModuleSubsystem());
        assertEquals("SwerveModuleSubsystem/FrontLeftDrive/", driveSubsystem.getFrontLeftSwerveModuleSubsystem().getPrefix());
        assertEquals("SwerveModuleSubsystem/FrontRightDrive/", driveSubsystem.getFrontRightSwerveModuleSubsystem().getPrefix());
        assertEquals("SwerveModuleSubsystem/RearLeftDrive/", driveSubsystem.getRearLeftSwerveModuleSubsystem().getPrefix());
        assertEquals("SwerveModuleSubsystem/RearRightDrive/", driveSubsystem.getRearRightSwerveModuleSubsystem().getPrefix());

        assertNotSame(
                driveSubsystem.getFrontLeftSwerveModuleSubsystem().getSteeringSubsystem(),
                driveSubsystem.getFrontRightSwerveModuleSubsystem().getSteeringSubsystem());
        assertEquals("SwerveSteeringSubsystem/FrontLeftDrive/", driveSubsystem.getFrontLeftSwerveModuleSubsystem().getSteeringSubsystem().getPrefix());
        assertEquals("SwerveSteeringSubsystem/FrontRightDrive/", driveSubsystem.getFrontRightSwerveModuleSubsystem().getSteeringSubsystem().getPrefix());
        assertEquals("SwerveSteeringSubsystem/RearLeftDrive/", driveSubsystem.getRearLeftSwerveModuleSubsystem().getSteeringSubsystem().getPrefix());
        assertEquals("SwerveSteeringSubsystem/RearRightDrive/", driveSubsystem.getRearRightSwerveModuleSubsystem().getSteeringSubsystem().getPrefix());

        assertNotSame(
                driveSubsystem.getFrontLeftSwerveModuleSubsystem().getDriveSubsystem(),
                driveSubsystem.getFrontRightSwerveModuleSubsystem().getDriveSubsystem());
        assertEquals("SwerveDriveSubsystem/FrontLeftDrive/", driveSubsystem.getFrontLeftSwerveModuleSubsystem().getDriveSubsystem().getPrefix());
        assertEquals("SwerveDriveSubsystem/FrontRightDrive/", driveSubsystem.getFrontRightSwerveModuleSubsystem().getDriveSubsystem().getPrefix());
        assertEquals("SwerveDriveSubsystem/RearLeftDrive/", driveSubsystem.getRearLeftSwerveModuleSubsystem().getDriveSubsystem().getPrefix());
        assertEquals("SwerveDriveSubsystem/RearRightDrive/", driveSubsystem.getRearRightSwerveModuleSubsystem().getDriveSubsystem().getPrefix());

        DriveSubsystem anotheDriveSubsystem = (DriveSubsystem)getInjectorComponent().driveSubsystem();
        assertSame(
                driveSubsystem,
                anotheDriveSubsystem);
        assertSame(
                driveSubsystem.getFrontLeftSwerveModuleSubsystem(),
                anotheDriveSubsystem.getFrontLeftSwerveModuleSubsystem());
        assertSame(
                driveSubsystem.getFrontLeftSwerveModuleSubsystem().getSteeringSubsystem(),
                anotheDriveSubsystem.getFrontLeftSwerveModuleSubsystem().getSteeringSubsystem());
        assertSame(
                driveSubsystem.getFrontLeftSwerveModuleSubsystem().getDriveSubsystem(),
                anotheDriveSubsystem.getFrontLeftSwerveModuleSubsystem().getDriveSubsystem());
    }

}