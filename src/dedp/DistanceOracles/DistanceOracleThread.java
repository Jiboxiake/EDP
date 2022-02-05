package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

public class DistanceOracleThread extends Thread {
    public ConnectedComponent cc=null;
    public PartitionVertex source =null;
    public PartitionVertex destination = null;
    public float distance = -1;
    @Override
    public void run(){
        assert(distance>0);
        try {
            cc.addDOEntry(source, destination, distance);
        }catch( ObjectNotFoundException e){
            System.out.println(e.getMessage());
        }
    }

    public void setCC(ConnectedComponent cc){
        this.cc=cc;
    }
    public void setParameters(PartitionVertex source, PartitionVertex destination, float distance){
        this.source=source;
        this.destination=destination;
        this.distance=distance;
    }


}
