package dedp.DistanceOracles;

import dedp.DistanceOracles.Precomputation.DiameterQueryEntry;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionVertex;

public class RuntimeQuadtreeDiameterThread extends Thread {
    PartitionVertex source;
    ConnectedComponent cc;
    @Override
    public void run(){
        DiameterQueryEntry entry = new DiameterQueryEntry();
        entry.cc = cc;
        entry.source = source;
        entry.computation();
    }
    public void setParameters(PartitionVertex source, ConnectedComponent cc){
        this.source=source;
        this.cc = cc;
    }
}
