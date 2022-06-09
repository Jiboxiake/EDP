package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.algorithms.Dijkstra;
import dedp.algorithms.hybridtraversal.DOTraversal;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.SPResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EDP_DO_vs_Dij {
    public static boolean coldStart=false;
    public static void load(EDP_DO_Test t, ArrayList<Integer>list) throws Exception {
        t.loadGraph(30000);
        for(int i=0; i<t.g.LabelsIDs.size();i++){
            list.add(i);
        }
        EDP_DO_Precomputation pre = new EDP_DO_Precomputation(t.index);
        File diameterFile = new File(PrecomputationResultDatabase.fileName);
        if(diameterFile.exists()){
            //System.out.println("esixts");
            DiameterLoader loader = new DiameterLoader(t.index, diameterFile);
            loader.load();
            loader=null;
        }else {
            pre.start_preprocessing();
            return;
        }
        if(EDP_DO_Test.precomputeDO){
            DOLoader.DOLoad(t.index);
        }
    }
    public static void main(String[]args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        ArrayList<Integer> list = new ArrayList<>();
        load(t,list);
        int p;
        Random generator = new Random();
        PartitionVertex randomSource;
        PartitionVertex randomDestination;
        if(!coldStart){
            for(int j=0; j<10000; j++){
                int from = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
                int to = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
                if(!t.g.containsVertex((long)from)||!t.g.containsVertex((long)to)){
                    continue;
                }
                DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
            }
        }
        Global.clearResult();
        for(int i=0; i<1; i++){
            int from = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
            int to = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
            if(!t.g.containsVertex((long)from)||!t.g.containsVertex((long)to)){
                i--;
                continue;
            }
            long startTime = System.nanoTime();
            SPResult r = DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
            long endTime   = System.nanoTime();
            double totalTime = (double)(endTime - startTime)/1000000000;
            System.out.println("EDP_DO explore total time is "+totalTime);
            Global.printResult();
            System.out.println("result is "+r.Distance);
            startTime = System.nanoTime();
            SPResult rr = Dijkstra.shortestDistance(t.g,from,to,list);
            endTime   = System.nanoTime();
            totalTime = (double)(endTime - startTime)/1000000000;
            System.out.println("Dij time is "+totalTime);
            System.out.println("true result is "+rr.Distance);
        }
    }
}
