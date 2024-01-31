package competition.subsystems.shooter;

import competition.subsystems.pose.PoseSubsystem;

import javax.inject.Singleton;
import java.lang.Math;

@Singleton
public class ShooterDistanceToRpmConverter {
    //IMPORTANT: BOTH ARRAYS NEED TO BE ORDERED IN INCREASING ORDER, EACH ELEMENT AT EACH INDEX CORRESPONDS TO THE SAME INDEX IN THE OTHER ARRAY
    //THINK OF IT LIKE A COORDINATE PAIR: rpmForDistance[0] has the RPM needed for the distance in distancesFromSpeaker[0]
    static double[] distancesFromSpeaker = {};
    //the values inputted here NEED TO BE TESTED ON THE ACTUAL ROBOT SHOOTER
    static double[] rpmForDistance = {};


    //estimates the slope needed for our distance based on prerecorded data
    public static double getSecantLineSlope(PoseSubsystem pose) {
        double secantLineSlope = 0;
        for (int i = 1; i < distancesFromSpeaker.length - 1; i++) {
            //logic to find where currentPosition lies in the array
            if (distancesFromSpeaker[i - 1] < pose.getDistanceFromSpeaker() && pose.getDistanceFromSpeaker() < distancesFromSpeaker[i + 1]) {
                //secant line calculator
                secantLineSlope =
                        (rpmForDistance[i + 1] - rpmForDistance[i - 1]) / (distancesFromSpeaker[i + 1] - distancesFromSpeaker[i - 1]);
            }
        }
        //returns ZERO if our current distance is further than the greatest range tested on the robot
        return secantLineSlope;
    }



}
