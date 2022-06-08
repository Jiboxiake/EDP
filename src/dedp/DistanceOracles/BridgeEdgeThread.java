package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.*;

public class BridgeEdgeThread extends Thread{
    private PartitionVertex source;
    //todo: change destination
    //private HashMap<Integer, PartitionVertex> destinations;
    private HashSet<Integer> destinations;
    private ConnectedComponent cc;
    private ArrayList<PartitionEdge> doBridgeEdgeList;
    private ArrayList<PartitionEdge> computedBridgeEdgeList;
    private int maxGuarantee;
    //Compute until destination is empty
   /* @Override
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
                //source.underBridgeComputation=false;
                source.lock.unlock();
                //handle DO
            }
        }
        //now this part has bug
        //distances from source to all vertices in this cc is computed.
        for(int i=0;i<bridgeEdgeList.size();i++){
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
        cc.addDO(partialDO);
        source.lock.lock();
        source.underBridgeComputation=false;
        this.bridgeEdgeList=null;
        this.destinations=null;
        source.lock.unlock();
    }*/
    @Override
    public void run(){
        int maxBridgeNum=-1;
        if(source.isBridge()){
            maxBridgeNum=cc.bridgeVerticesSize()-1;
        }else{
            maxBridgeNum=cc.bridgeVerticesSize();
        }
        if(maxBridgeNum==0){
            source.allBridgeEdgesComputed=true;
            return;
        }
        source.lock.lock();
        source.underBridgeComputation=true;
        source.lock.unlock();
        RuntimeQuadtreeDiameterThread diameterThread = null;
        if(!source.isBridge()){
            diameterThread = new RuntimeQuadtreeDiameterThread();
            diameterThread.setParameters(source,cc);
            diameterThread.start();;
        }
        VertexQueueEntry entry = new VertexQueueEntry(source,0);
        PriorityQueue<VertexQueueEntry>q = new PriorityQueue<>();
        HashMap <Integer, VertexQueueEntry> distMap = new HashMap<>();
        distMap.put(source.getId(), entry);
        q.add(entry);
        boolean newEntryAdded=false;
        HashMap<SearchKey, Float> partialDO = new HashMap<>();
        while(!q.isEmpty()){
            newEntryAdded=false;
            VertexQueueEntry en = q.poll();
            if(destinations.contains(en.vertex.getId())){
                PartitionEdge e = new PartitionEdge();
                e.setFrom(source);
                e.setTo(en.vertex);
                e.setWeight(en.distance);
                e.setLabel(source.Label);
                computedBridgeEdgeList.add(e);//computed list will be naturally sorted
                newEntryAdded=true;
            }
            for(int i=maxGuarantee;i<doBridgeEdgeList.size();i++){//max guarantee determines how many do entries are available
                if(doBridgeEdgeList.get(i).getWeight()<=en.distance){
                    newEntryAdded=true;
                    maxGuarantee++;
                }else{
                    break;
                }
            }
            if(newEntryAdded){//either computed list becomes available or do list has more entries available or both.
                source.lock.lock();
                source.numOfBridgeEdgesComputed=maxGuarantee+computedBridgeEdgeList.size();
                source.bridgeEdgeAdded.signalAll();
                source.lock.unlock();
            }
            for(PartitionEdge e: en.vertex.outEdges){//the Dijkstra part, updated to use O(n) time
                PartitionVertex to = e.getTo();
 /*               if(to.getId()==14218&&cc.partition.Label==0){
                    System.out.println("current cc contains 14218 "+cc.vertices.containsKey(14218));
                }*/
                if(e.getLabel()!=source.Label){
                    throw new RuntimeException("error with wrong label");
                }
                if(!cc.vertices.containsKey(to.getId())){
                    try {
                        throw new ObjectNotFoundException("vertex "+to.getId()+" not exist in cc");
                    } catch (ObjectNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
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
            if(doBridgeEdgeList.size()+computedBridgeEdgeList.size()>=maxBridgeNum){
                source.lock.lock();
                source.numOfBridgeEdgesComputed=doBridgeEdgeList.size()+computedBridgeEdgeList.size();
                source.allBridgeEdgesComputed=true;
                source.bridgeEdgeAdded.signalAll();
                source.lock.unlock();
                break;
            }
        }
     /*   DOBridgeBufferEntry doEntry = new DOBridgeBufferEntry(source, computedBridgeEdgeList,distMap);
        if(!cc.sendBridgeDOWork(doEntry)){
            //do it yourself
            for(int i=0; i<computedBridgeEdgeList.size();i++){
                PartitionEdge pe = computedBridgeEdgeList.get(i);
                PartitionVertex destination = pe.getTo();
                SearchKey key = new SearchKey(source.mc, destination.mc);
                if(needInsertion(partialDO,key)){
                    try {
                        key = cc.optimizedSearchKeyGeneration(distMap,source,destination,pe.getWeight());
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    partialDO.put(key,pe.getWeight());
                }
            }
            cc.addDO(partialDO);
        }*/
        //always do it yourself
        //just assume we ignore the source as it can be anywhere
        if(source.isBridge()){

        }else{
            try {
                diameterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(int i=0; i<computedBridgeEdgeList.size();i++){
            PartitionEdge pe = computedBridgeEdgeList.get(i);
            PartitionVertex destination = pe.getTo();
            //for debug
            SearchKey key = new SearchKey(source.morton(), destination.morton());
            if(needInsertion(partialDO,key)){
                try {
                    key = cc.optimizedSearchKeyGeneration(source, destination, pe.getWeight());
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                }
                partialDO.put(key,pe.getWeight());
            }
        }
        cc.addDO(partialDO);
       /* try {
            Thread.sleep(999999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        partialDO=null;
        source.lock.lock();
        source.underBridgeComputation=false;
       // doBridgeEdgeList=null;
        distMap=null;
       // computedBridgeEdgeList=null;
        source.lock.unlock();
        return;
    }




    public boolean needInsertion(HashMap<SearchKey, Float> partialDO, SearchKey key) {
        //for debug
        SearchKey copy = new SearchKey(key);
        for (int i = 0; i < MortonCode.max_depth; i++) {
            if (partialDO.containsKey(key)) {
                if (partialDO.get(key) < 0) {
                    throw new RuntimeException("wrong DO entry got inserted\n");
                }
                if(Global.debug){
                    Global.debug=false;
                    //copy.printBit();
                    //key.printBit();
                    //System.out.println("match");
                }
                //Global.DO_hit();
                return false;
            }
            key.shift();
        }
        return true;
    }

    public ArrayList<PartitionEdge>getBridgeEdgeList(){
        //return this.bridgeEdgeList;
        return this.doBridgeEdgeList;
    }
    public void setParameters(ConnectedComponent cc,PartitionVertex source,HashSet<Integer> destinations, ArrayList<PartitionEdge> doBridgeEdgeList, ArrayList<PartitionEdge> computedBridgeEdgeList, int maxGuarantee){
       // this.bridgeEdgeList=bridgeEdgeList;
        this.doBridgeEdgeList=doBridgeEdgeList;
        this.computedBridgeEdgeList= computedBridgeEdgeList;
        this.source=source;
        this.destinations=destinations;
        this.cc=cc;
        this.maxGuarantee=maxGuarantee;
    }

    public void copyList(ArrayList<PartitionEdge> doBridgeEdgeList, ArrayList<PartitionEdge> computedBridgeEdgeList){
        doBridgeEdgeList=this.doBridgeEdgeList;
        computedBridgeEdgeList=this.computedBridgeEdgeList;
    }

    public ArrayList<PartitionEdge> copyDOList(){
        return this.doBridgeEdgeList;
    }

    public ArrayList<PartitionEdge> copyComputedList(){
        return this.computedBridgeEdgeList;
    }
   
}
