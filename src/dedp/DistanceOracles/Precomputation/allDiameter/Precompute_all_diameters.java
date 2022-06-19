package dedp.DistanceOracles.Precomputation.allDiameter;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.DistanceOracles.QuadTree;
import dedp.algorithms.Dijkstra;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.SPResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Precompute_all_diameters {
    public static int total_workers = 40;
    public static void compputeDiameter(EDP_DO_Test t) throws InterruptedException {
        allDiameterThread[] workers = new allDiameterThread[total_workers];
        for(int i=0; i<total_workers;i++){
            workers[i] = new allDiameterThread();
        }
        int size = 30000;
        int amount;
        if(size%total_workers==0){
            amount = size/total_workers;
        }else{
            amount = size/total_workers+1;
        }
        for(int i=0;i<total_workers;i++){
            int low =i*amount;
            int high = low + amount -1;
            if(high>(size-1)){
                high = size -1;
                workers[i].high=high;
                workers[i].low = low;
                workers[i].t = t;
                break;
            }
            workers[i].high=high;
            workers[i].low = low;
            workers[i].t = t;
        }
        for(int i=0;i<total_workers;i++){
            workers[i].start();
        }
        for(int i=0;i<total_workers;i++){
            workers[i].join();
        }
        prepareDiameterOutput(t);
        PrecomputationResultDatabase.output();
    }

    public static void prepareDiameterOutput(EDP_DO_Test t){
        Partition p;
        ConnectedComponent cc;
        for(int i=0; i<t.index.getNumOfPartitions(); i++){
            p = t.index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                cc = p.ConnectedComponents.getConnectedComponent(j);
                if(cc.vertices==null){
                    continue;
                }
                cc.tree.output();
            }
        }
    }
    public static void main(String[]args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        File diameterFile = new File(PrecomputationResultDatabase.fileName);
        if(diameterFile.exists()){
            //System.out.println("esixts");
            DiameterLoader loader = new DiameterLoader(t.index, diameterFile);
            loader.load();
        }else{
            compputeDiameter(t);
        }
        //compputeDiameter(t);
        //now let's do some random test
   /*     for(int i=0; i<1000; i++){
            int id = ThreadLocalRandom.current().nextInt(0,30000);
            while(!t.g.containsVertex(id)){
                id = ThreadLocalRandom.current().nextInt(0,30000);
            }
            for(int j=0; j<t.index.partitions.length;j++){
                Partition p = t.index.getPartition(j);
                if(!p.containsVertex(id)){
                    continue;
                }
                PartitionVertex v = p.getVertex(id);
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(v.ComponentId);
                QuadTree sourceTree = cc.tree;
                for(int d=0; d<QuadTree.initial_depth;d++){
                    sourceTree = sourceTree.containingBlock(v);
                }

            }
        }*/
        for(int j=0; j<t.index.partitions.length;j++){
            Partition p = t.index.getPartition(j);
            for(int i=0; i<p.ConnectedComponents.getConnectedComponentsCount();i++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(i);
                boolean result =cc.tree.testDiameter();
                if(!result){
                    System.out.println("Error of diameter at partition "+j+" cc "+i);
                }
            }
        }
    }
}
