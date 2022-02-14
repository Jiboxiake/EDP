package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BridgeDOThread extends Thread{
    private Partition partition;
    private HashMap<Integer, ArrayList<PartitionEdge>> vertexToBridge;
    @Override
    public void run(){
        HashMap<Integer,ArrayList<DOEntry>>ccToDOs=new HashMap<>();
        for(Map.Entry<Integer, ArrayList<PartitionEdge>>set:vertexToBridge.entrySet()){
            ArrayList<PartitionEdge>bridgeList=set.getValue();
            int sourceID = set.getKey();
            PartitionVertex source=null;
            try {
                source = partition.getVertex(sourceID);
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
            while(!source.allBridgeEdgesComputed){
                try {
                    source.bridgeEdgeAdded.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<DOEntry> DOList=null;
            if(!ccToDOs.containsKey(source.ComponentId)){
                DOList=new ArrayList<>();
                ccToDOs.put(source.ComponentId,DOList);
            }else{
                DOList=ccToDOs.get(source.ComponentId);
            }
            ConnectedComponent cc = partition.ConnectedComponents.getConnectedComponent(source.ComponentId);
            for(int i=0; i< bridgeList.size();i++){
                PartitionEdge edge = bridgeList.get(i);
                if(edge.getFrom().getId()!=sourceID){
                    try {
                        throw new ObjectNotFoundException("id not match");
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                PartitionVertex to = edge.getTo();
                float distance = edge.getWeight();
                try {
                    DOEntry entry = cc.getEntry(source, to, distance);
                    DOList.add(entry);
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        for(Map.Entry<Integer,ArrayList<DOEntry>>set:ccToDOs.entrySet()){
            int ccID = set.getKey();
            ConnectedComponent cc = partition.ConnectedComponents.getConnectedComponent(ccID);
            ArrayList<DOEntry>doEntries = set.getValue();
            cc.addEntryList(doEntries);
        }
    }

    public void setParameters(Partition p, HashMap<Integer, ArrayList<PartitionEdge>> vertexToBridge){
        this.partition=p;
        this.vertexToBridge=vertexToBridge;
    }
}
