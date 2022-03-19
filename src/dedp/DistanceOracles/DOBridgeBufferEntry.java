package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;
import java.util.HashMap;

public class DOBridgeBufferEntry {
    public PartitionVertex source;
    public ArrayList<PartitionEdge> bridgeEdges;
    public HashMap<Integer, VertexQueueEntry> DistanceMap;

    public DOBridgeBufferEntry(PartitionVertex source, ArrayList<PartitionEdge> bridgeEdges, HashMap<Integer, VertexQueueEntry> DistanceMap){
        this.source=source;
        this.bridgeEdges=bridgeEdges;
        this.DistanceMap = DistanceMap;
    }
}
