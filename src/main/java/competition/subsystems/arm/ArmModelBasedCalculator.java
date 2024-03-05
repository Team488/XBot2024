package competition.subsystems.arm;

import xbot.common.math.MathUtils;

import javax.inject.Inject;

public class ArmModelBasedCalculator {

    @Inject
    public ArmModelBasedCalculator() {
        // Empty for now, but we may have properties later for tuning/adjusting some constants or offsets.
    }

    public double getExtensionForSpeakerDistance(double distanceFromSpeaker) {
        return getArmExtensionForAngle(getArmAngleFromDistance(distanceFromSpeaker));
    }

    public double getArmAngleFromDistance(double distanceFromSpeaker) {
        // Get arm angle (in degrees) from distance from speaker (in meters)
        double k0 = 9.085E+01 ;
        double k1 = -3.922E+01 ;
        double k2 = 7.291E+00 ;
        double k3 = -4.877E-01 ;
        double angle = ( k0 + k1 * distanceFromSpeaker + k2 * Math.pow(distanceFromSpeaker, 2) + k3 * Math.pow(distanceFromSpeaker, 3)) ;
        if (angle > 54.7 ){
            angle = 54.7 ;
        }
        return ( angle) ;
    }

    public double getArmAngleForExtension(double extensionDistance) {
        // Get desired shooting angle (in degrees) from extension length (in mm)
        double  b0 = 5.463E+01 ;
        double b1 = -4.589E+02 ;
        double b2 = 1.034E+03 ;
        double b3 = -3.378E+03 ;
        double extension_meters = extensionDistance / 1000.0 ;
        double angle_degrees = ( b0 + b1 * extension_meters + b2 * Math.pow(extension_meters, 2) + b3 * Math.pow(extension_meters, 3)) ;
        return ( angle_degrees) ;
    }

    public double getArmExtensionForAngle(double armAngle) {
        // Get extension length (in mm) from desired shooting angle (degrees)
        double a0 = 1.432E-01 ;
        double a1 = -2.702E-03 ;
        double a2 = -5.286E-06 ;
        double a3 = 1.218E-07 ;
        double extension_meters = ( a0 + a1 * armAngle + a2 * Math.pow(armAngle, 2) + a3 * Math.pow(armAngle, 3)) ;
        double extension_mm = ( extension_meters * 1000.0) ;
        // Any value over 150mm isn't useful, as that's a "flat shot" that can't possibly
        // score in the Speaker.
        return extension_mm ;
    }
}
