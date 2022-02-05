package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class BridgeEdgeDOThread extends Thread{
    private PartitionVertex source;
    private HashMap<Integer, PartitionVertex> destinations;
    private ConnectedComponent cc;
    private ArrayList<PartitionEdge> bridgeEdgeList;
    private int maxGuarantee;
    //Compute until destination is empty
    @Override
    public void run(){
        source.lock.lock();
        //do not ever start 2 computation of the same vertex
        if(source.underBridgeComputation){
            return;
        }
        source.underBridgeComputation=true;
        source.lock.unlock();
        VertexQueueEntry entry = new VertexQueueEntry(source,0);
        PriorityQueue<VertexQueueEntry>q = new PriorityQueue<>();
        HashMap <Integer, VertexQueueEntry> distMap = new HashMap<>();
        distMap.put(source.getId(), entry);
        q.add(entry);
        while(!q.isEmpty()){
            VertexQueueEntry en = q.poll();
            //add new entry to the DO and list
            if(destinations.containsKey(en.vertex.getId())){
                PartitionEdge e = new PartitionEdge();
                e.setFrom(source);
                e.setTo(en.vertex);
                e.setWeight(en.distance);
                bridgeEdgeList.add(e);
                Collections.sort(bridgeEdgeList);
                try {
                    cc.addDOEntry(source, en.vertex, en.distance);
                } catch (ObjectNotFoundException a) {
                    a.printStackTrace();
                }
            }
            //check if the first maxGuarantee many bridge edges are computed
            if(en.vertex.isBridge()){
                if(bridgeEdgeList.get(maxGuarantee).getWeight()<en.distance){
                    source.numOfBridgeEdgesComputed=maxGuarantee;
                    maxGuarantee++;
                    //signal waiting thread
                }
            }
            for(int i=0; i<en.vertex.outEdges.size();i++){
                PartitionEdge e = en.vertex.outEdges.get(i);
                PartitionVertex to = e.getTo();
                VertexQueueEntry toEn = distMap.get(to.getId());
                if(toEn==null){
                    toEn=new VertexQueueEntry(to, en.distance+e.getWeight());
                    distMap.put(to.getId(),toEn);
                    q.add(toEn);
                }else if(toEn.distance>e.getWeight()+en.distance){
                    toEn.distance=e.getWeight()+en.distance;
                    q.remove(toEn);
                    q.add(toEn);
                }
            }
            //if we have fully computed distance to all bridge vertices, we
            if(bridgeEdgeList.size()==cc.bridgeVerticesSize()){
                source.bridgeEdgeAdded.signalAll();
                //Collections.sort(bridgeEdgeList);
                return;
            }
        }
    }

    public ArrayList<PartitionEdge>getBridgeEdgeList(){
        return this.bridgeEdgeList;
    }
    public void setParameters(ConnectedComponent cc,PartitionVertex source,HashMap<Integer, PartitionVertex>destinations, ArrayList<PartitionEdge> bridgeEdgeList, int maxGuarantee){
        this.bridgeEdgeList=bridgeEdgeList;
        this.source=source;
        this.destinations=destinations;
        this.cc=cc;
        this.maxGuarantee=maxGuarantee;
    }
}
