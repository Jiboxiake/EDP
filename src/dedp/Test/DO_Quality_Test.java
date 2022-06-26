package dedp.Test;

import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.DistanceOracles.SearchKey;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DO_Quality_Test {
    public static void main(String[]args) throws Exception {
        dedp.DistanceOracles.EDP_DO_Test t = new dedp.DistanceOracles.EDP_DO_Test();
        t.loadGraph(30000);//set a bound on how many vertices we want
        ArrayList<Integer> list = new ArrayList<>();
        System.out.println("total partition vertex number is "+Global.total_partition_vertex);
        System.out.println("total partition edge number is "+Global.total_partition_edge);
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
        DOLoader.DOLoad(t.index);
    /*    for(int i=0; i<t.index.getNumOfPartitions();i++){
            Partition p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                if(!cc.completeDOQualityTest()){
                    System.out.println("Partition "+i+" Connected component "+j+" failed the DO quality test");
                }
            }
        }*/
        ConnectedComponent cc0 = t.index.partitions[0].ConnectedComponents.getConnectedComponent(0);
        PartitionVertex v1 = cc0.vertices.get(3);
        PartitionVertex v2 = cc0.getVertex(4);
        SearchKey key = new SearchKey(v1.morton(),v2.morton());
        key.printBit();
        cc0.noLockLookUp(v1,v2);
        System.out.println("max error is "+Global.maxError);
    }
}
