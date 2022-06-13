package dedp.DistanceOracles.Precomputation.allDiameter;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.DiameterQueryEntry;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.RuntimeQuadtreeDiameterThread;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

public class allDiameterThread extends Thread {
    public int low;
    public int high;
    public EDP_DO_Test t;
    @Override
    public void run(){
        Partition p;
        ConnectedComponent cc=null;
        PartitionVertex v=null;
        QuadTree sourceTree = null;
        for(int i=low; i<=high;i++){
            //check whether all diameters are computed for this vertex.
            for(int j=0; j<t.index.partitions.length;j++){
                p = t.index.getPartition(j);
                if(!p.containsVertex(i)){
                    continue;
                }
                try {
                    v = p.getVertex(i);
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                }
                cc = p.ConnectedComponents.getConnectedComponent(v.ComponentId);
                sourceTree = cc.tree;
                for(int d=0; d<QuadTree.initial_depth;d++){
                    sourceTree = sourceTree.containingBlock(v);
                }
                while(true){
                    if(sourceTree==null||sourceTree.getLevel()==QuadTree.max_depth){
                        break;
                    }
                    if(sourceTree.getDiameter()<0){
                        DiameterQueryEntry entry = new DiameterQueryEntry();
                        entry.cc = cc;
                        entry.source=v;
                        entry.computation();
                        break;
                    }else{
                        sourceTree = sourceTree.containingBlock(v);
                    }
                }
            }
        }
    }
}
