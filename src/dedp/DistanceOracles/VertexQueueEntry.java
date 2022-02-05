package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.PartitionVertex;

public class VertexQueueEntry implements Comparable<VertexQueueEntry>{
    public PartitionVertex vertex;
    public float distance;
    public VertexQueueEntry(PartitionVertex v, float distance){
        this.vertex=v;
        this.distance=distance;
    }

    @Override
    public int compareTo(VertexQueueEntry o) {
        return Float.compare(this.distance, o.distance);
    }
}
