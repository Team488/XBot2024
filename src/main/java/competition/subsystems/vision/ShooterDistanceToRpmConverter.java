package competition.subsystems.vision;

import competition.subsystems.pose.PoseSubsystem;

public class ShooterDistanceToRpmConverter {
    //IMPORTANT: BOTH ARRAYS NEED TO BE ORDERED IN INCREASING ORDER, EACH ELEMENT AT EACH INDEX CORRESPONDS TO THE SAME INDEX IN THE OTHER ARRAY
    //THINK OF IT LIKE A COORDINATE PAIR: rpmForDistance[0] has the RPM needed for the distance in distancesFromSpeaker[0]
    double[] distancesFromSpeaker = {};
    double[] rpmForDistance = {};
    PoseSubsystem pose;
    public ShooterDistanceToRpmConverter(PoseSubsystem pose){
        this.pose = pose;
    }
    public static double getSecantLineSlope(){
        for (int i = 1; ){
            
        }
    }




}
