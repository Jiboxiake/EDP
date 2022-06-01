package dedp.DistanceOracles.MonochromeDO;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BridgeDOThread extends Thread {
    private ConnectedComponent cc;
    public ArrayList<ConnectedComponent> workloads=new ArrayList<>();

    @Override
    public void run() {
        for(int i=0; i<workloads.size();i++){
            cc = workloads.get(i);
            try {
                cc.outputDO();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
