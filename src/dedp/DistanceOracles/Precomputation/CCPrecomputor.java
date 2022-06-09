package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.HybridDOEDPIndex;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.Map;

public class CCPrecomputor {
    private DiameterComputationThread[] workers;
    public HybridDOEDPIndex index;
    public int total_workers;
    //parallel computation of diameter as needed

    public CCPrecomputor(HybridDOEDPIndex index) {
        this.index = index;
        int total_ver = (int)index.PlainGraph.getVertexCount();
        total_workers = 60;//can be customized to be dependent on total ver
        workers = new DiameterComputationThread[total_workers];
        for(int i=0; i<total_workers; i++){
            workers[i]= new DiameterComputationThread();
        }
    }
    public void startComputation() throws InterruptedException {
        int counter =0;
        Partition p=null;
        ConnectedComponent cc = null;
        //partition the workload
        for(int i=0; i<index.getNumOfPartitions(); i++){
            p = index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                cc = p.ConnectedComponents.getConnectedComponent(j);
                if(cc.bridgeVertices==null){
                    continue;
                }
                for(Map.Entry<Integer, PartitionVertex>set:cc.bridgeVertices.entrySet()){
                    counter = counter%total_workers;
                    PartitionVertex bridge = set.getValue();
                    DiameterQueryEntry entry = new DiameterQueryEntry();
                    entry.cc = cc;
                    entry.source = bridge;
                    workers[counter].workloads.add(entry);
                    counter++;
                }
            }
        }
        for(int i=0; i<total_workers; i++){
            workers[i].start();
        }
        for(int i=0; i<total_workers; i++){
            workers[i].join();
        }
     /*   for(int i=0; i<index.getNumOfPartitions(); i++){
            p = index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                cc = p.ConnectedComponents.getConnectedComponent(j);
                if(cc.bridgeVertices==null){
                    continue;
                }
                cc.tree.output();
            }
        }*/
    }

}
