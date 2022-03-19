package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionVertex;

public class DistanceOracleDirectThread extends Thread{
    public ConnectedComponent cc;

    @Override
    public void run(){

    }

    public void setParameter(ConnectedComponent cc){
        this.cc=cc;
    }

}
