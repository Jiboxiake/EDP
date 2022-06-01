package dedp.DistanceOracles.MonochromeDO;

import dedp.DistanceOracles.HybridDOEDPIndex;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

import java.io.IOException;

public class DOLoader {
    public static void DOLoad(HybridDOEDPIndex index) throws IOException {
        Partition p;
        ConnectedComponent cc;
        for(int i=0; i<index.getNumOfPartitions();i++){
            p = index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                cc = p.ConnectedComponents.getConnectedComponent(j);
                cc.inputDO();
            }
        }
    }
}
