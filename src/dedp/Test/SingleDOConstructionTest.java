package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.ALLDO.AllDOPrecomputation;
import dedp.DistanceOracles.Precomputation.ALLDO.AllDOThread;
import dedp.DistanceOracles.Precomputation.ALLDO.AllDOWorkloadEntry;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.DistanceOracles.Precomputation.allDiameter.Precompute_all_diameters;
import dedp.DistanceOracles.QuadTree;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.io.File;
import java.util.ArrayList;

public class SingleDOConstructionTest {
    public static void main(String[]args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        File diameterFile = new File(PrecomputationResultDatabase.fileName);
        if(diameterFile.exists()){
            //System.out.println("esixts");
            DiameterLoader loader = new DiameterLoader(t.index, diameterFile);
            loader.load();
        }else{
            Precompute_all_diameters.compputeDiameter(t);
        }
        ArrayList <Integer> list = new ArrayList<>();
        list.add(0);
        Partition p = t.index.getPartition(0);
        ConnectedComponent cc = t.index.partitions[0].ConnectedComponents.getConnectedComponent(0);
        ArrayList<QuadTree> initialLevelBlocks = new ArrayList<>();
        cc.tree.getAllInitialLevelBlocks(initialLevelBlocks);
        System.out.println("initial block number is "+initialLevelBlocks.size());
        ArrayList<AllDOWorkloadEntry> allPairs = AllDOPrecomputation.createPairs(initialLevelBlocks,p,cc);
        System.out.println("total pairs have "+allPairs.size());
        //System.out.println(BidirectionalDijkstra.shortestDistance(t.g,4,3,list).Distance);
        AllDOThread thread = new AllDOThread();
        thread.t=t;
        thread.workloads = allPairs;
        thread.start();
        thread.join();
        PartitionVertex v1 = cc.getVertex(3);
        PartitionVertex v2 = cc.getVertex(4);
        System.out.println(cc.noLockLookUp(v1,v2));
        //cc.printDO();
    }
}
