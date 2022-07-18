package dedp.DistanceOracles.FlatBuffer;

import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Output_CC_ID {
    public static void main(String args[]) throws Exception {
        dedp.DistanceOracles.EDP_DO_Test t = new dedp.DistanceOracles.EDP_DO_Test();
        t.loadGraph(300000);//set a bound on how many vertices we want
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
        String pathname = "./allCCNames.txt";;
        FileWriter fileWriter = new FileWriter(pathname);
        String name;
        for(int i=0; i<t.index.getNumOfPartitions();i++){
            Partition p = t.index.getPartition(i);
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                if(cc.vertices!=null&&cc.vertices.size()>1){
                    name = i+"_"+j+".txt\n";
                    //System.out.print(name);
                    fileWriter.write(name);
                }
                boolean test = cc.checkDOFile();
                if(!test){
                    System.out.println("CC "+j+" in partition "+i+" has error");
                }
            }
        }
        fileWriter.close();
    }
}
