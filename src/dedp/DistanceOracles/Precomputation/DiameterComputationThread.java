package dedp.DistanceOracles.Precomputation;

import dedp.indexes.edgedisjoint.ConnectedComponent;

import java.util.ArrayList;
import java.util.HashSet;

public class DiameterComputationThread extends Thread {
    public ArrayList<DiameterQueryEntry> workloads = new ArrayList<>();
    @Override
    public void run(){
        DiameterQueryEntry entry = null;
        for(int i=0; i<workloads.size();i++){
            entry = workloads.get(i);
            entry.computation();
        }
    }
}
