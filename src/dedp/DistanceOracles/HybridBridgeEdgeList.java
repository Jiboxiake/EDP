package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;
//do we want to handle everything including waiting and checking in this class?
public class HybridBridgeEdgeList {
    private ArrayList<PartitionEdge> DOList;
    private ArrayList<PartitionEdge> computedList;
    private PartitionVertex source;
    private int DOIndex=0;
    private int computedIndex=0;
    private int maxBridgeEdgeNum=0;
    private int edgeOrder=0;
    public HybridBridgeEdgeList(PartitionVertex source, ConnectedComponent cc){
        this.source=source;
        DOList=new ArrayList<>();
        computedList=new ArrayList<>();
        if(source.isBridge()){
            this.maxBridgeEdgeNum = cc.bridgeVerticesSize()-1;
        }else{
            this.maxBridgeEdgeNum = cc.bridgeVerticesSize();
        }
    }

    public int getEdgeOrder(){
        return this.edgeOrder;
    }

    //todo: check correctness here
    public PartitionEdge getEdge() throws InterruptedException {
        if(edgeOrder!=DOIndex+computedIndex){
            throw new RuntimeException("Error at getting partition edge in special list\n");
        }
        PartitionEdge result = null;
        source.lock.lock();
        while(!source.allBridgeEdgesComputed&&source.numOfBridgeEdgesComputed<=edgeOrder){
            source.bridgeEdgeAdded.await();
        }
        // now we can get one
        if(edgeOrder<source.numOfBridgeEdgesComputed){
            if(computedList.size()==computedIndex){
                result = DOList.get(DOIndex);
                DOIndex++;
                edgeOrder++;
                source.lock.unlock();
                return result;
            }else if (DOList.size()==DOIndex){
                result = computedList.get(computedIndex);
                computedIndex++;
                edgeOrder++;
                source.lock.unlock();
                return result;
            }
            //now we get to decide which one to pick next
            PartitionEdge e1 = DOList.get(DOIndex);
            PartitionEdge e2 = computedList.get(computedIndex);
            //pick the shorter one
            if(e1.getWeight()>=e2.getWeight()){
                DOIndex++;
                edgeOrder++;
                source.lock.unlock();
                return e1;
            }else{
                computedIndex++;
                edgeOrder++;
                source.lock.unlock();
                return e2;
            }
        }
        source.lock.unlock();
        return result;
    }

    public void setParameters(ArrayList<PartitionEdge> DOList,ArrayList<PartitionEdge> computedList){
        this.DOList=DOList;
        this.computedList=computedList;
    }

}
