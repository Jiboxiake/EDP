package dedp.DistanceOracles.FlatBuffer;

import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

public class DO_FlatBuffer_Transfer {
    public static void main(String[]args) throws Exception {
        dedp.DistanceOracles.EDP_DO_Test t = new dedp.DistanceOracles.EDP_DO_Test();
        t.loadGraph(30000);
        DOLoader.DOLoad(t.index);
        for(int i=0; i<t.index.partitions.length;i++){
            Partition p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                if(i==0&&j==1){
                    cc.serialize();
                }
            }
        }
    }
}
