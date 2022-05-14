package dedp.Test;

import dedp.DistanceOracles.BridgeEdgeThread;
import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.SearchKey;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.io.File;
import java.util.*;

public class Bridge_edge_test {
    public static void main(String args[]) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        File diameterFile = new File(PrecomputationResultDatabase.fileName);
        if(diameterFile.exists()){
            DiameterLoader loader = new DiameterLoader(t.index, diameterFile);
            loader.load();
            loader=null;
        }else {
            return;
        }
        ConnectedComponent cc = t.index.partitions[1].ConnectedComponents.getConnectedComponent(3);
        QuadTree tree = t.index.partitions[1].ConnectedComponents.getConnectedComponent(3).tree;
        PartitionVertex source =cc.vertices.get(18173);
        source.thread=new BridgeEdgeThread();
        source.underBridgeComputation=true;
        source.numOfBridgeEdgesComputed=0;
        HashSet<Integer> bridgeVertices = new HashSet<>();
        for(Map.Entry<Integer,PartitionVertex>set:cc.bridgeVertices.entrySet()){
            bridgeVertices.add(set.getKey());
        }
        ArrayList<PartitionEdge> doList= new ArrayList<>();
        ArrayList<PartitionEdge> computedList = new ArrayList<>();
        source.thread.setParameters(cc, source, bridgeVertices, doList, computedList, 0);
        source.thread.start();
        source.thread.join();
        Object[] vecs = (cc.bridgeVertices.values().toArray());
        Random generator = new Random();
        PartitionVertex destination =  (PartitionVertex) vecs[generator.nextInt(vecs.length)];
        //SearchKey key = new SearchKey(source.morton(),destination.morton());
        float result = cc.noLockLookUp(source,destination);
        System.out.println(result);
    }
}
