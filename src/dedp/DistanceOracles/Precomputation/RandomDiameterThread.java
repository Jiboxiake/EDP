package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.HybridDOEDPIndex;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.RuntimeQuadtreeDiameterThread;
import dedp.algorithms.hybridtraversal.DOTraversal;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RandomDiameterThread extends Thread {
    public EDP_DO_Test t;
    public static int totalRuns=1000;
    public static int totalWorkers=60;
    @Override
    public void run(){
        ArrayList<RuntimeQuadtreeDiameterThread> workers = new ArrayList<>();
        for(int i=0; i<totalRuns;i++){
            int id = ThreadLocalRandom.current().nextInt(0, 271450 + 1);
            if(!t.g.containsVertex(id)){
                continue;
            }
            for(int j=0; j<t.index.partitions.length;j++){
                Partition p = t.index.partitions[j];
                if(p.containsVertex(id)){
                    try {
                        PartitionVertex v = p.getVertex(id);
                        computeAll(v,p);
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public void computeAll(PartitionVertex v, Partition p){
        ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(v.ComponentId);
        QuadTree tree = cc.tree;
        boolean needCompute = false;
        for(int d=0; d<QuadTree.initial_depth;d++){
            tree = tree.containingBlock(v);
        }
        while(true){
            if(tree==null||tree.getLevel()==QuadTree.max_depth){
                break;
            }
            if(tree.getDiameter()<0){
                needCompute=true;
                break;
            }else{
                tree = tree.containingBlock(v);
            }
        }
        if(needCompute){
            DiameterQueryEntry entry = new DiameterQueryEntry();
            entry.cc = cc;
            entry.source = v;
            entry.computation();
        }
    }
}
