package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.*;

public class BridgeEdgeThread extends Thread{
    private PartitionVertex source;
    //todo: change destination
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
        HashMap<SearchKey, Float> partialDO = new HashMap<>();
        while(!q.isEmpty()){
            VertexQueueEntry en = q.poll();
            //System.out.println(en);
            //add new entry to the DO and list
            if(destinations.containsKey(en.vertex.getId())){
                //System.out.println("found bridge vertex "+en.vertex.getId());
                //System.out.println(bridgeEdgeList.size());
                PartitionEdge e = new PartitionEdge();
                e.setFrom(source);
                e.setTo(en.vertex);
                e.setWeight(en.distance);
                bridgeEdgeList.add(e);
                Collections.sort(bridgeEdgeList);
           /*     source.lock.lock();
                source.numOfBridgeEdgesComputed++;
                source.lock.unlock();
                try {
                    float check=cc.lookUp(source, en.vertex);
                    if(check<0){
                        cc.addDOEntry(source, en.vertex, en.distance);
                        Global.addBridge_do_count();
                    }
                } catch (ObjectNotFoundException a) {
                    a.printStackTrace();
                }*/
            }
            //check if the first maxGuarantee many bridge edges are computed
            //todo:check if this is correct
            while(bridgeEdgeList.size()>maxGuarantee&&bridgeEdgeList.get(maxGuarantee).getWeight()<en.distance){
                    maxGuarantee++;
                    source.lock.lock();
                    source.numOfBridgeEdgesComputed=maxGuarantee;
                    //todo: signal waiting thread
                    source.bridgeEdgeAdded.signalAll();
                    source.lock.unlock();
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
            if(bridgeEdgeList.size()==cc.bridgeVerticesSize()||(source.isBridge()&&bridgeEdgeList.size()==cc.bridgeVertices.size()-1)){
                source.lock.lock();
                source.numOfBridgeEdgesComputed=bridgeEdgeList.size();
                source.allBridgeEdgesComputed=true;
                source.bridgeEdgeAdded.signalAll();
                source.underBridgeComputation=false;
                source.lock.unlock();
                //handle DO
            }
        }
        //now this part has bug
        //distances from source to all vertices in this cc is computed.
    /*    for(int i=0;i<bridgeEdgeList.size();i++){
            PartitionEdge pe = bridgeEdgeList.get(i);
            PartitionVertex to = pe.getTo();
            if(pe.getFrom().getId()!=source.getId()){
                throw new RuntimeException("id not match");
            }
            SearchKey key = new SearchKey(source.mc, to.mc);
            if(needInsertion(partialDO, key)){
                try {
                    key = cc.optimizedSearchKeyGeneration(distMap,source,to,pe.getWeight());
                    partialDO.put(key,pe.getWeight());
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                Global.addDO_hit_during_bridge_computation();
            }
        }
        cc.addDO(partialDO);*/
        this.bridgeEdgeList=null;
        this.destinations=null;
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
