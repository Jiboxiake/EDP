package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DistanceOracleBridgeThread extends Thread {
    public ConnectedComponent cc=null;
    public PartitionVertex source =null;
    public HashMap<Integer, VertexQueueEntry> distanceMap;
    public ArrayList<PartitionEdge> bridgeEdgeList;
    public final Lock lock = new ReentrantLock();
    public final Condition doEntryAdded = lock.newCondition();
    public float distance = -1;
    public Queue<DOBridgeBufferEntry> q = new LinkedList<>();
    public HashMap<SearchKey, Float>partialDO=new HashMap<>();
    @Override
    public void run(){
        while(true){
            lock.lock();
            while(q.isEmpty()){
                try {
                    doEntryAdded.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            DOBridgeBufferEntry entry = q.poll();
            source = entry.source;
            bridgeEdgeList=entry.bridgeEdges;
            distanceMap=entry.DistanceMap;
            for(int i=0;i<bridgeEdgeList.size();i++){
                PartitionEdge pe = bridgeEdgeList.get(i);
                PartitionVertex destination = pe.getTo();
                SearchKey key = new SearchKey(source.mc, destination.mc);
                if(needInsertion(partialDO,key)){
                    try {
                        key = cc.optimizedSearchKeyGeneration(distanceMap,source,destination,pe.getWeight());
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    partialDO.put(key,pe.getWeight());
                }
            }
            cc.addDO(partialDO);
            partialDO.clear();
            bridgeEdgeList=null;
            distanceMap=null;
            entry=null;
        }
    }

    public void setCC(ConnectedComponent cc){
        this.cc=cc;
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

}
