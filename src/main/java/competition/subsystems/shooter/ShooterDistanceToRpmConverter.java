package competition.subsystems.shooter;

import competition.subsystems.pose.PoseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.Math;

public class ShooterDistanceToRpmConverter {
    //IMPORTANT: BOTH ARRAYS NEED TO BE ORDERED IN INCREASING ORDER, EACH ELEMENT AT EACH INDEX CORRESPONDS TO THE SAME INDEX IN THE OTHER ARRAY
    //THINK OF IT LIKE A COORDINATE PAIR: rpmForDistance[0] has the RPM needed for the distance in distancesFromSpeaker[0]
    double[] distancesFromSpeaker = {};
    //the values inputted here NEED TO BE TESTED ON THE ACTUAL ROBOT SHOOTER
    double[] rpmForDistance = {};
    PoseSubsystem pose;

    //the only purpose for the contructor is for testing, its not required outside of testing
    @Inject
    public ShooterDistanceToRpmConverter(double[] distancesFromSpeaker, double[] rpmForDistance, PoseSubsystem pose){
        this.distancesFromSpeaker = distancesFromSpeaker;
        this.rpmForDistance = rpmForDistance;
        this.pose = pose;
    }

    public ShooterDistanceToRpmConverter(){
        this.distancesFromSpeaker = getDistancesFromSpeakerArray();
        this.rpmForDistance = getRpmForDistanceArray();
    }


    //estimates the slope needed for our distance based on prerecorded data
    public double getRPMForDistance(double distanceFromSpeaker) {
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
        return secantLineSlope * pose.getDistanceFromSpeaker();
    }

    public double[] getRpmForDistanceArray() {
        return rpmForDistance;
    }

    public double[] getDistancesFromSpeakerArray() {
        return distancesFromSpeaker;
    }
}
