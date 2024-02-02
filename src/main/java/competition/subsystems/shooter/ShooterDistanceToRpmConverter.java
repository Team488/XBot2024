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


    //the only purpose for the contructor is for testing, its not required outside of testing
    public ShooterDistanceToRpmConverter(double[] distancesFromSpeaker, double[] rpmForDistance){
        this.distancesFromSpeaker = distancesFromSpeaker;
        this.rpmForDistance = rpmForDistance;
    }

    public ShooterDistanceToRpmConverter(){
        this.distancesFromSpeaker = getDistancesFromSpeakerArray();
        this.rpmForDistance = getRpmForDistanceArray();
    }

    //why am i doing algebra :sob:
    public double getYIntercept(double slope, double x1, double y1){
        return  y1 - slope * x1;
    }


    //estimates the slope needed for our distance based on prerecorded data
    public double getRPMForDistance(double distanceFromSpeaker) {
        double secantLineSlope = 0;
        double yIntercept = 0;
        for (int i = 0; i <= distancesFromSpeaker.length - 1; i++) {
            //logic to find where currentPosition lies in the array
            if (distancesFromSpeaker[i] == distanceFromSpeaker){
                return rpmForDistance[i];
            }
            else if (distancesFromSpeaker[i] < distanceFromSpeaker && distanceFromSpeaker < distancesFromSpeaker[i + 1]) {
                //secant line calculator
                secantLineSlope =
                        (rpmForDistance[i + 1] - rpmForDistance[i]) / (distancesFromSpeaker[i + 1] - distancesFromSpeaker[i]);
                yIntercept = getYIntercept(secantLineSlope,distancesFromSpeaker[i],rpmForDistance[i]);
                break;
            }
        }
        //returns ZERO if our current distance is further than the greatest range tested on the robot
        return secantLineSlope * distanceFromSpeaker + yIntercept;
    }

    public double[] getRpmForDistanceArray() {
        return rpmForDistance;
    }

    public double[] getDistancesFromSpeakerArray() {
        return distancesFromSpeaker;
    }
}
