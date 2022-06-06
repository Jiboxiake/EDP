package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.algorithms.Dijkstra;
import dedp.algorithms.hybridtraversal.DOTraversal;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.SPResult;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MonochromeDistanceOracleTest {
    public static void main(String[]args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        ArrayList<Integer> list = new ArrayList<>();
        for(int i=0; i<t.g.LabelsIDs.size();i++){
            list.add(i);
        }
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
        if(EDP_DO_Test.precomputeDO){
            DOLoader.DOLoad(t.index);
        }
        int p;
        Random generator = new Random();
        PartitionVertex randomSource;
        PartitionVertex randomDestination;

        int ccID;
  /*      for(int i=0; i<0; i++){
            //pick a random partition
            p = ThreadLocalRandom.current().nextInt(0, list.size());
            Partition partition = t.index.getPartition(p);
            ccID = ThreadLocalRandom.current().nextInt(0, partition.ConnectedComponents.getConnectedComponentsCount() );
            ConnectedComponent cc = partition.ConnectedComponents.getConnectedComponent(ccID);
            if(cc.bridgeVertices==null||cc.bridgeVertices.size()==0){
                continue;
            }
            Object[] bridgeVertices = cc.bridgeVertices.values().toArray();
            randomSource =(PartitionVertex) bridgeVertices[generator.nextInt(bridgeVertices.length)];
            randomDestination =(PartitionVertex) bridgeVertices[generator.nextInt(bridgeVertices.length)];
            if(randomSource.getId()==randomDestination.getId()){
                continue;
            }
            float doResult = cc.noLockLookUp(randomSource,randomDestination);
            SPResult dijResult = Dijkstra.shortestDistance(partition,randomSource.getId(),randomDestination.getId());
            float error  = Math.abs(100*(dijResult.Distance-doResult)/dijResult.Distance);
            if(error>25){
                System.out.println("DO: Source is "+randomSource.getId()+" destination is "+randomDestination.getId()+" Shortest distance = " + doResult);
                System.out.println("DIJ: Source is "+randomSource.getId()+" destination is "+randomDestination.getId()+" Shortest distance = " + dijResult.Distance);
                System.out.println("error is "+error);
            }
        }*/
    /*    for(int j=0; j<0;j++){
            p = ThreadLocalRandom.current().nextInt(0, list.size());
            Partition partition = t.index.getPartition(p);
            Object[] vertices = partition.bridgeVertexes.toArray();
            int sourceID =(Integer) vertices[generator.nextInt(vertices.length)];
            int destinationID =(Integer) vertices[generator.nextInt(vertices.length)];
            randomSource = partition.getVertex(sourceID);
            randomDestination = partition.getVertex(destinationID);
            ccID = randomSource.ComponentId;
            ConnectedComponent cc = partition.ConnectedComponents.getConnectedComponent(ccID);
            if(randomSource.getId()==randomDestination.getId()){
                continue;
            }
            float doResult = cc.noLockLookUp(randomSource,randomDestination);
            SPResult dijResult = Dijkstra.shortestDistance(partition,randomSource.getId(),randomDestination.getId());
            float error  = Math.abs(100*(dijResult.Distance-doResult)/dijResult.Distance);
            if(error>30){
                System.out.println("DO: Source is "+randomSource.getId()+" destination is "+randomDestination.getId()+" Shortest distance = " + doResult);
                System.out.println("DIJ: Source is "+randomSource.getId()+" destination is "+randomDestination.getId()+" Shortest distance = " + dijResult.Distance);
                System.out.println("error is "+error);
            }
        }*/
        int count=0;
        for(int z=0; z<list.size();z++){
            Partition partition = t.index.partitions[z];
            for(int i=0; i<partition.ConnectedComponents.getConnectedComponentsCount();i++){
                ConnectedComponent cc = partition.ConnectedComponents.getConnectedComponent(i);
                if(cc.bridgeVertices==null||cc.bridgeVertices.size()==0){
                    continue;
                }
                for(Map.Entry<Integer,PartitionVertex>set:cc.bridgeVertices.entrySet()){
                    PartitionVertex source = set.getValue();
                    for(Map.Entry<Integer,PartitionVertex>dSet:cc.bridgeVertices.entrySet()){
                        PartitionVertex destination = dSet.getValue();
                        if(source.getId()==destination.getId()){
                            continue;
                        }
                        float doResult = cc.noLockLookUp(source,destination);
                        SPResult dijResult = Dijkstra.shortestDistance(partition,source.getId(),destination.getId());
                        float error  = Math.abs(100*(dijResult.Distance-doResult)/dijResult.Distance);
                        count++;
                        if(error>25){
                            System.out.println("DO: Source is "+source.getId()+" destination is "+destination.getId()+" Shortest distance = " + doResult);
                            System.out.println("DIJ: Source is "+source.getId()+" destination is "+destination.getId()+" Shortest distance = " + dijResult.Distance);
                            System.out.println("error is "+error);
                        }
                    }
                }
            }
        }
        System.out.println("Count is "+count);
   /*     FileWriter myWriter = new FileWriter("result.txt");
        long startTime = System.nanoTime();
        double total=0;
        double max_err=-100;
        float error=0;
        float error2=0;
        for(int from=0; from<30000; from++){
            if(!t.g.containsVertex((long)from)){
                continue;
            }
            for(int to=0; to<30000; to++){
                if(!t.g.containsVertex((long)to)){
                    continue;
                }
                if(from==to){
                    continue;
                }
                String result="";
                //EDP_DO_Test_Thread th = new EDP_DO_Test_Thread();
                SPResult r = DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
                SPResult rr = Dijkstra.shortestDistance(t.g,from,to,list);
                if(r.Distance==rr.Distance&&r.Distance==-1){
                    //System.out.println("Source "+from+" destination "+to+" cannot reach each other");
                    result+="Source "+from+" destination "+to+" cannot reach each other\n";
                }else{
                    error = Math.abs(100*(r.Distance-rr.Distance)/rr.Distance);
                    if(error>max_err){
                        max_err=error;
                    }
                    total+=error;
                    //System.out.println("EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance);
                    result+="EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance+"\n";
                    //System.out.println("Dijkstra: Source is "+from+" destination is "+to+" Shortest distance = " + rr.Distance);
                    result+="Dijkstra: Source is "+from+" destination is "+to+" Shortest distance = " + rr.Distance+"\n";
                    //System.out.println("error is "+error+"%");
                    result+="error is "+error+"%\n";
                }
                for(int z = 0; z< Global.list.size(); z++){
                    Global.list.get(z).join();
                }
                Global.list.clear();
                r = DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
                error2 = Math.abs(100*(r.Distance-rr.Distance)/rr.Distance);
                if(error2>max_err){
                    max_err=error2;
                }
                //System.out.println("Second run EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance);
                result+="Second run EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance+"\n";
                //System.out.println("error 2 is "+error2+"%");
                result+="error 2 is "+error2+"%\n";
                total+=error2;

                //System.out.println(result);
                if(error>20||error2>20){
                    myWriter.write(result);
                }
                error=0;
                error2=0;
            }
        }*/
    }
}
