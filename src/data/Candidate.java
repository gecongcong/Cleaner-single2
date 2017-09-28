package data;

/**
 * Created by gcc on 17-9-28.
 */
public class Candidate {
    public String candidate = "";
    public int cost = 9999;
    public int tupleID = -1;

    public Candidate(int tupleID,String candidate,int cost){
        this.candidate = candidate;
        this.cost = cost;
        this.tupleID = tupleID;
    }
}
