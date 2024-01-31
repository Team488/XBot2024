package competition.subsystems.vision;

import competition.subsystems.pose.PoseSubsystem;
import java.lang.Math;

public class ShooterDistanceToRpmConverter {
    //IMPORTANT: BOTH ARRAYS NEED TO BE ORDERED IN INCREASING ORDER, EACH ELEMENT AT EACH INDEX CORRESPONDS TO THE SAME INDEX IN THE OTHER ARRAY
    //THINK OF IT LIKE A COORDINATE PAIR: rpmForDistance[0] has the RPM needed for the distance in distancesFromSpeaker[0]
    static double[] distancesFromSpeaker = {};
    static double[] rpmForDistance = {};


    //estimates the slope needed for our distance based on prerecorded data
    public static double getSecantLineSlope(PoseSubsystem pose) {
        double secantLineSlope;
        for (int i = 1; i < distancesFromSpeaker.length - 1; i++) {
            //logic to find where currentPosition lies in the array
            if (distancesFromSpeaker[i - 1] < pose.getDistanceFromSpeaker() && pose.getDistanceFromSpeaker() < distancesFromSpeaker[i + 1]) {
                //secant line calculator
                secantLineSlope =
                        (rpmForDistance[i + 1] - rpmForDistance[i - 1]) / (distancesFromSpeaker[i + 1] - distancesFromSpeaker[i - 1]);
            }
        }
        //experiment with rounding the numbers
        return secantLineSlope;
    }



}
