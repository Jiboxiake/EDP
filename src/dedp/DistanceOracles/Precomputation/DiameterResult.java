package dedp.DistanceOracles.Precomputation;

public class DiameterResult implements Comparable<DiameterResult>{
    public int quadtreeID;
    public float diameter;
    public int representativeID;
    public DiameterResult(int id, float diameter, int representativeID){
        this.quadtreeID=id;
        this.diameter=diameter;
        this.representativeID = representativeID;
    }

    @Override
    public int compareTo(DiameterResult o) {
        return Float.compare(this.diameter, o.diameter);
    }
    @Override
    public String toString() {
        return "Current tree is "+quadtreeID+" with diameter "+diameter;
    }
}
