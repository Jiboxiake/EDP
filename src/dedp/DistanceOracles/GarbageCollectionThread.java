package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.PartitionEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GarbageCollectionThread extends Thread{
    public HashMap<Integer, HashMap<Integer, ArrayList<PartitionEdge>>> partitionVertexBridgeEdges;
    public void setParameter(HashMap<Integer, HashMap<Integer, ArrayList<PartitionEdge>>> partitionVertexBridgeEdges){
        this.partitionVertexBridgeEdges=partitionVertexBridgeEdges;
    }
    @Override
    public void run(){
        for(Map.Entry<Integer, HashMap<Integer, ArrayList<PartitionEdge>>>set:this.partitionVertexBridgeEdges.entrySet()){
            for(Map.Entry<Integer, ArrayList<PartitionEdge>>pair:set.getValue().entrySet()){

            }
        }
    }

}
