package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.MonochromeDO.MonochromeDOCreation;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

import java.io.File;
import java.util.ArrayList;

public class Preprocessing {
    public static void main(String[]args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(300000);
        EDP_DO_Precomputation pre = new EDP_DO_Precomputation(t.index);
        pre.start_preprocessing();
        MonochromeDOCreation.precomputeBridgeDO(t);
        ArrayList<RandomDiameterThread>workerList = new ArrayList<>();
        for(int i=0;i<RandomDiameterThread.totalWorkers;i++){
            RandomDiameterThread thread = new RandomDiameterThread();
            thread.t=t;
            workerList.add(thread);
            thread.start();
        }
        for(int i=0;i<RandomDiameterThread.totalWorkers;i++){
            workerList.get(i).join();
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
                if(cc.bridgeVertices==null){
                    continue;
                }
                cc.tree.output();
            }
        }
    }
}
