package dedp.DistanceOracles.Analytical;

public class CCInfoCOntainer implements Comparable<CCInfoCOntainer> {
    public int ccID;
    public int partitionID;
    public int vertexSize;
    public int edgeSize;
    public int score;

    public CCInfoCOntainer(int ccID,int partitionID, int vertexSize, int edgeSize){
        this.ccID=ccID;
        this.partitionID=partitionID;
        this.vertexSize=vertexSize;
        this.edgeSize=edgeSize;
        this.score=vertexSize+edgeSize;
    }

    @Override
    public int compareTo(CCInfoCOntainer o) {
        return Integer.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "Current CC has partition "+this.partitionID+" with ID "+this.ccID+" with "+this.vertexSize+" vertices and "+this.edgeSize+" edges";
    }
}
