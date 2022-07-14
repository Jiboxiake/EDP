package dedp.Test;

import dedp.DistanceOracles.EdgeLabelProcessor;
import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.io.File;
import java.util.ArrayList;

public class WSPD_Test {
    public static void main(String[]args) throws Exception {
        dedp.DistanceOracles.EDP_DO_Test t = new dedp.DistanceOracles.EDP_DO_Test();
        t.loadGraph(30000);//set a bound on how many vertices we want
        ArrayList<Integer> list = new ArrayList<>();
        for(int i=0; i<t.g.LabelsIDs.size()/2;i++){
            list.add(i);
        }
        System.out.println("total number of do threads are "+ Global.total_do_threads);
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
        for(int i=0; i<t.index.getNumOfPartitions();i++){
            Partition p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                if(!cc.testDO(1)||!cc.WSPD_Reverse_Test()){
                    System.out.println("Partition "+i+" Connected component "+j+" failed the WSPD test");
                }
            }
        }
      /*  Partition p = t.index.getPartition(1);
        ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(3);
        PartitionVertex source = p.getVertex(510);
        PartitionVertex destination = p.getVertex(4108);
        float result = cc.noLockLookUp(source,destination);
        System.out.println(result);*/
        System.out.println("Total WSPD pass is "+Global.WSPD_Pass);
        System.out.println("Total WSPD fail is "+Global.WSPD_Fail);
    }
}
