package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;
//do we want to handle everything including waiting and checking in this class?
public class hybridBridgeEdgeList {
    private ArrayList<PartitionEdge> DOList;
    private ArrayList<PartitionEdge> computedList;
    private PartitionVertex source;
    private int DOIndex=0;
    private int computedIndex=0;
    private int maxBridgeEdgeNum=0;
    private int edgeOrder=0;
    public hybridBridgeEdgeList(PartitionVertex source, ConnectedComponent cc){
        this.source=source;
        DOList=new ArrayList<>();
        computedList=new ArrayList<>();
        if(source.isBridge()){
            maxBridgeEdgeNum = cc.bridgeVerticesSize()-1;
        }else{
            maxBridgeEdgeNum = cc.bridgeVerticesSize();
        }
    }

    public int getEdgeOrder(){
        return this.edgeOrder;
    }

    public PartitionEdge getEdge() throws InterruptedException {
        if(edgeOrder!=DOIndex+computedIndex){
            throw new RuntimeException("Error at getting partition edge in special list\n");
        }
        PartitionEdge result = null;
        source.lock.lock();
        while(!source.allBridgeEdgesComputed&&source.numOfBridgeEdgesComputed<=edgeOrder){
            source.bridgeEdgeAdded.await();
        }
        // nowe we can get one
        if(edgeOrder<source.numOfBridgeEdgesComputed){
            if(computedList.size()==computedIndex){
                result = DOList.get(DOIndex);
                DOIndex++;
                edgeOrder++;
                return result;
            }else if (DOList.size()==DOIndex){
                result = computedList.get(computedIndex);
                computedIndex++;
                edgeOrder++;
                return result;
            }
            //now we get to decide which one to pick next
            PartitionEdge e1 = DOList.get(DOIndex);
            PartitionEdge e2 = computedList.get(computedIndex);
            //pick the shorter one
            if(e1.getWeight()>=e2.getWeight()){
                DOIndex++;
                edgeOrder++;
                return e1;
            }else{
                computedIndex++;
                edgeOrder++;
                return e2;
            }
        }
        return result;
    }


}
