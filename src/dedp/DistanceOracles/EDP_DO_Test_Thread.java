package dedp.DistanceOracles;

import dedp.algorithms.hybridtraversal.HybridTraversal;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.SPResult;

import java.util.ArrayList;

public class EDP_DO_Test_Thread extends Thread {
    public HybridDOEDPIndex index;
    public int from;
    public int to;
    private int id;
    private ArrayList<Integer> allowedLabels;

    @Override
    public void run() {
        try {
            SPResult r= HybridTraversal.shortestDistanceWithEdgeDisjointDistanceOracle(index, from, to, allowedLabels);
            System.out.println("task "+id+" Shortest distance = " + r.Distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParameter(int id, int from, int to, ArrayList<Integer> allowedLabels,HybridDOEDPIndex index) {
        this.from = from;
        this.index=index;
        this.to = to;
        this.id = id;
        this.allowedLabels=allowedLabels;
    }
}