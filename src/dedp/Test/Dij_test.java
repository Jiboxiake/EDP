package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.algorithms.Dijkstra;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.structures.SPResult;

import java.util.ArrayList;

public class Dij_test {
    public static void main(String[] args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(10000);
        ArrayList<Integer> list = new ArrayList<>();
        for(int i=0; i<t.g.LabelsIDs.size();i++){
            list.add(i);
        }
        SPResult result = BidirectionalDijkstra.shortestDistance(t.g,4188,697,list);
        System.out.println(result.Distance);
    }
}
