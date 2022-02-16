package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BridgeDOThread extends Thread {
    private ConnectedComponent cc;
    private HashMap<Integer, ArrayList<PartitionEdge>> vertexToBridge;

    @Override
    public void run() {
        ArrayList<PartitionEdge> vertexBridgeEntries = null;
        HashMap<SearchKey, Float> partialDO = new HashMap<>();
        for (Map.Entry<Integer, ArrayList<PartitionEdge>>set:vertexToBridge.entrySet()) {
            int sourceID = set.getKey();
            PartitionVertex source = cc.vertices.get(sourceID);
            //todo: further optimization here of smart ordering
            while(!source.allBridgeEdgesComputed){
                try {
                    source.bridgeEdgeAdded.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            vertexBridgeEntries=set.getValue();
            for (int i = 0; i < vertexBridgeEntries.size(); i++) {
                PartitionEdge e = vertexBridgeEntries.get(i);
                //PartitionVertex source = e.getFrom();
                PartitionVertex destination = e.getTo();
                SearchKey key = new SearchKey(source.mc, destination.mc);
                if (needInsertion(partialDO, key)) {
                    try {
                        //todo: a potential optimization is to use a worker thread to compute this
                        key = cc.getSearchKey(source, destination, e.getWeight());
                        partialDO.put(key, e.getWeight());
                    } catch (ObjectNotFoundException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Global.addDO_hit_during_bridge_computation();
                }
            }
        }
        cc.addDO(partialDO);
    }

    public boolean needInsertion(HashMap<SearchKey, Float> partialDO, SearchKey key) {
        for (int i = 0; i < 33; i++) {
            if (partialDO.containsKey(key)) {
                if (partialDO.get(key) < 0) {
                    throw new RuntimeException("wrong DO entry got inserted\n");
                }
                Global.DO_hit();
                return false;
            }
            key.shift();
        }
        return true;
    }

    public void setParameters(ConnectedComponent cc, HashMap<Integer, ArrayList<PartitionEdge>> vertexToBridge) {
        this.cc = cc;
        this.vertexToBridge = vertexToBridge;
        //this.ccBridgeEntries=ccBridgeEntries;
    }
}
