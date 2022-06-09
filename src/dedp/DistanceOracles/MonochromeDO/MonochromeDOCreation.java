package dedp.DistanceOracles.MonochromeDO;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MonochromeDOCreation {
    public static final int numThread=60;
    public static void computeBridgeDO() throws Exception {
        BridgeDOThread[] doThread = new BridgeDOThread[numThread];
        for(int i=0; i<numThread;i++){
            doThread[i] = new BridgeDOThread();
        }
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
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
        int id=0;
        Partition p;
        //ConnectedComponent cc;
        for(int i=0; i<t.index.partitions.length;i++){
            p=t.index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                id = id%numThread;
                doThread[id].workloads.add(cc);
                id++;
            }
        }
        for(int i=0;i<numThread;i++){
            doThread[i].start();
        }
        for(int i=0; i<numThread;i++){
            doThread[i].join();
        }
    }

    public static void precomputeBridgeDO(EDP_DO_Test t) throws Exception {
        BridgeDOThread[] doThread = new BridgeDOThread[numThread];
        for(int i=0; i<numThread;i++){
            doThread[i] = new BridgeDOThread();
        }
        int id=0;
        Partition p;
        //ConnectedComponent cc;
        for(int i=0; i<t.index.partitions.length;i++){
            p=t.index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                id = id%numThread;
                doThread[id].workloads.add(cc);
                id++;
            }
        }
        for(int i=0;i<numThread;i++){
            doThread[i].start();
        }
        for(int i=0; i<numThread;i++){
            doThread[i].join();
        }
        System.out.println("bridge DO computation finished");
    }

    public static void main(String[]args) throws Exception {
        MonochromeDOCreation.computeBridgeDO();

    }
}
