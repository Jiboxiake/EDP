package dedp.Test;

import dedp.DistanceOracles.*;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.algorithms.Dijkstra;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.SPResult;

import java.io.File;
import java.util.*;

public class Bridge_edge_test {
    public static void main(String args[]) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        System.out.println(Parser.max_lat);
        System.out.println(Parser.min_lat);
        System.out.println(Parser.max_long);
        System.out.println(Parser.min_long);
        File diameterFile = new File(PrecomputationResultDatabase.fileName);
        if(diameterFile.exists()){
            DiameterLoader loader = new DiameterLoader(t.index, diameterFile);
            loader.load();
            loader=null;
        }else {
            return;
        }
        ArrayList<Integer>list= new ArrayList<>();
        list.add(1);
        ConnectedComponent cc = t.index.partitions[1].ConnectedComponents.getConnectedComponent(3);
        QuadTree tree = t.index.partitions[1].ConnectedComponents.getConnectedComponent(3).tree;
        PartitionVertex source =cc.vertices.get(18173);
        source.thread=new BridgeEdgeThread();
        source.underBridgeComputation=true;
        source.numOfBridgeEdgesComputed=0;
        HashSet<Integer> bridgeVertices = new HashSet<>();
        for(Map.Entry<Integer,PartitionVertex>set:cc.bridgeVertices.entrySet()){
            bridgeVertices.add(set.getKey());
        }
        ArrayList<PartitionEdge> doList= new ArrayList<>();
        ArrayList<PartitionEdge> computedList = new ArrayList<>();
        source.thread.setParameters(cc, source, bridgeVertices, doList, computedList, 0);
        source.thread.start();
        source.thread.join();
        Object[] vecs = (cc.bridgeVertices.values().toArray());
        Random generator = new Random();
        float total_err=0;
        float max_err=0;
        //SearchKey key = new SearchKey(source.morton(),destination.morton());
        for(int x=0; x<1; x++){
            //PartitionVertex destination =  (PartitionVertex) vecs[generator.nextInt(vecs.length)];
            PartitionVertex destination = cc.vertices.get(11989);
            float result = cc.noLockLookUp(source,destination);
            SPResult rr = Dijkstra.shortestDistance(t.g, source.getId(),destination.getId(),list);
            float error = Math.abs(rr.Distance-result)/rr.Distance*100;
            if(error>max_err){
                max_err=error;
            }
            total_err+=error;
            if(error>5){
                System.out.println("error is "+error+"% "+destination.getId());//12951
            }

        }
        //System.out.println("avg error is "+total_err/1000+"%");
        System.out.println("max error is "+max_err+"%");
        //System.out.println(result);
    }
}
