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
        source.underBridgeComputation=true;
        source.lock.unlock();
        VertexQueueEntry entry = new VertexQueueEntry(source,0);
        PriorityQueue<VertexQueueEntry>q = new PriorityQueue<>();
        HashMap <Integer, VertexQueueEntry> distMap = new HashMap<>();
        distMap.put(source.getId(), entry);
        q.add(entry);
        while(!q.isEmpty()){
            VertexQueueEntry en = q.poll();
            //System.out.println(en);
            //add new entry to the DO and list
            if(destinations.containsKey(en.vertex.getId())){
                destinations.remove(en.vertex.getId());
                //System.out.println("found bridge vertex "+en.vertex.getId());
                //System.out.println(bridgeEdgeList.size());
                PartitionEdge e = new PartitionEdge();
                e.setFrom(source);
                e.setTo(en.vertex);
                e.setWeight(en.distance);
                bridgeEdgeList.add(e);
                Collections.sort(bridgeEdgeList);
                try {
                    float check=cc.lookUp(source, en.vertex);
                    if(check<0){
                        cc.addDOEntry(source, en.vertex, en.distance);
                        Global.addBridge_do_count();
                    }
                } catch (ObjectNotFoundException a) {
                    a.printStackTrace();
                }
            }
            //check if the first maxGuarantee many bridge edges are computed
            //todo:check if this is correct
            if(bridgeEdgeList.size()>maxGuarantee&&bridgeEdgeList.get(maxGuarantee).getWeight()<en.distance){
                    maxGuarantee++;
                    source.numOfBridgeEdgesComputed=maxGuarantee;
                    //todo: signal waiting thread
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
                source.lock.lock();
                source.bridgeEdgeAdded.signalAll();
                source.underBridgeComputation=false;
                source.lock.unlock();
                System.out.println("exit");
                //Collections.sort(bridgeEdgeList);
                return;
            }
        }
        System.err.println("error");
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
