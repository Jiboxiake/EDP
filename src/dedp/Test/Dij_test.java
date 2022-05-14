package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.algorithms.Dijkstra;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.structures.SPResult;

import java.util.ArrayList;

public class Dij_test {
    public static void main(String[] args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(300000);
        ArrayList<Integer> list = new ArrayList<>();
      /*  for(int i=0; i<t.g.LabelsIDs.size();i++){
            list.add(i);
        }*/
        list.add(1);
        SPResult result = BidirectionalDijkstra.shortestDistance(t.g,1380,52430,list);
        SPResult r2 = Dijkstra.shortestDistance(t.index.partitions[1],1380,52430);//my dij is correct then
        System.out.println(result.Distance);
        System.out.println(r2.Distance);
    }
}
