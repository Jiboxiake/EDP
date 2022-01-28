package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;

public class EDP_DO_Test_Thread extends Thread {
    public Partition p;
    public int from;
    public int to;
    private int id;
    private ArrayList<Integer> allowedLabels;

    @Override
    public void run() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPartition(Partition p) {
        this.p = p;
    }

    public void setParameter(int id, int from, int to, ArrayList<Integer> allowedLabels) {
        this.from = from;
        this.to = to;
        this.id = id;
        this.allowedLabels=allowedLabels;
    }
}