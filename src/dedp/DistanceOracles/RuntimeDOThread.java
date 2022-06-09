package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionVertex;

public class RuntimeDOThread extends Thread {
    public ConnectedComponent cc;
    public PartitionVertex source;
    public PartitionVertex destination;
    float result;
    public void setCC(ConnectedComponent cc){
        this.cc = cc;
    }
    public void setParameters(PartitionVertex u, PartitionVertex v, float result){
        this.result =result;
        this.source =u;
        this.destination = v;
    }
    @Override
    public void run(){//todo: right now ignore concurrency, but based on WSPD, not a huge problem?
        try {
            SearchKey key  =  cc.optimizedSearchKeyGeneration(source,destination,result);
            cc.addSingleDO(key,result);
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
    }
}
