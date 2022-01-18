package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

public class DOTestThread extends Thread {
    public Partition p;
    public int from;
    public int to;
    private int id;

    @Override
    public void run(){
        try {
           System.out.println("id is "+id+" "+p.getEdgeWeightDO(from, to));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setPartition(Partition p){
        this.p=p;
    }
    public void setParameter(int id, int from, int to){
        this.from=from;
        this.to=to;
        this.id=id;
    }




}
