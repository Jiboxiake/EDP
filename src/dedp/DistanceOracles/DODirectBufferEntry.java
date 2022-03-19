package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.DistanceFromSource;

import java.util.HashMap;
import java.util.Map;

public class DODirectBufferEntry {
    public PartitionVertex source;
    public PartitionVertex destination;
    public float distance;
    public HashMap<Long, DistanceFromSource> distMap;
    public DODirectBufferEntry(PartitionVertex source, PartitionVertex destination, float distance, HashMap<Long, DistanceFromSource> distMap){
        this.source=source;
        this.destination=destination;
        this.distance=distance;
        this.distMap=distMap;
    }
}
