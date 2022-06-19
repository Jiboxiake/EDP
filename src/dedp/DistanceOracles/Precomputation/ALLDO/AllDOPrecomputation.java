package dedp.DistanceOracles.Precomputation.ALLDO;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.DistanceOracles.Precomputation.allDiameter.Precompute_all_diameters;
import dedp.DistanceOracles.QuadTree;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//nah we can do it with directed graph as well, but now we only care about indirect ones
//todo: test this part
public class AllDOPrecomputation {
    //assume diameters are all already loaded.
    public static int total_workers=60;
    public static void precomputeAllDO(EDP_DO_Test t) throws InterruptedException, IOException {

        AllDOThread[] workers = new AllDOThread[total_workers];
        for(int i=0;i<workers.length;i++){
            workers[i]= new AllDOThread();
            workers[i].t = t;
        }
        Partition p = null;
        ConnectedComponent cc = null;
        for(int i=0; i<t.index.getNumOfPartitions();i++){
            p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                cc = p.ConnectedComponents.getConnectedComponent(j);
                ArrayList<QuadTree> initialLevelBlocks = new ArrayList<>();
                cc.tree.getAllInitialLevelBlocks(initialLevelBlocks);
                ArrayList<AllDOWorkloadEntry> allPairs = createPairs(initialLevelBlocks,p,cc);
                for(int z=0;z<allPairs.size();z++){
                    workers[z%total_workers].workloads.add(allPairs.get(z));
                }
            }
        }
        for(int z=0;z<workers.length;z++){
            workers[z].start();
        }
        for(int z=0;z<workers.length;z++){
            workers[z].join();
        }
        System.out.println("precomputation of DO finished");
        for(int i=0; i<t.index.getNumOfPartitions();i++){
            p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                cc = p.ConnectedComponents.getConnectedComponent(j);
                cc.outputDO();
            }
        }
    }

    public static ArrayList<AllDOWorkloadEntry> createPairs(ArrayList<QuadTree> initialLevelBlocks, Partition p, ConnectedComponent cc){
        ArrayList<AllDOWorkloadEntry> pairList = new ArrayList<>();
        for(int i=0; i<initialLevelBlocks.size();i++){
            QuadTree t1 = initialLevelBlocks.get(i);
            for(int j=i; j<initialLevelBlocks.size();j++){
                QuadTree t2 = initialLevelBlocks.get(j);
                AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
                entry.partition=p;
                entry.cc = cc;
                entry.t1 = t1;
                entry.t2 = t2;
                pairList.add(entry);
            }
        }
        return pairList;
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
            Precompute_all_diameters.compputeDiameter(t);
        }
        long startTime = System.nanoTime();
        AllDOPrecomputation.precomputeAllDO(t);
        long endTime   = System.nanoTime();
        double totalTime = (double)(endTime - startTime)/1000000000;
        String name = "DOComputationStats.txt";
        FileWriter fileWriter = new FileWriter(name);
        String result ="";
        result+="Total DIJ run is "+PreprocessingGlobal.total_dij_run+"\n";
        result+="Total queue insertion is "+PreprocessingGlobal.total_queue_insertion+"\n";
        for(int i=0; i<PreprocessingGlobal.doLevel.length;i++){
            result+="Do created at level "+i+" is "+PreprocessingGlobal.doLevel[i]+"\n";
        }
        fileWriter.write(result);
        //System.out.println("total time is "+totalTime);

     /*   for(int i=0; i<t.index.getNumOfPartitions();i++){
            Partition p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                cc.outputDO();
            }
        }*/
        //let's do some random test later
    }
}
