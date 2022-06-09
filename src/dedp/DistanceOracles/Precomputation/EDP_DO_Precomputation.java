package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.HybridDOEDPIndex;

import java.util.ArrayList;

public class EDP_DO_Precomputation {
    HybridDOEDPIndex index;
    public EDP_DO_Precomputation(HybridDOEDPIndex index){
        this.index= index;
    }
    public void start_preprocessing(){
        CCPrecomputor computor = new CCPrecomputor(this.index);
        try {
            computor.startComputation();
            System.out.println("diameter computation finished");
            //PrecomputationResultDatabase.print(30);
            //PrecomputationResultDatabase.output();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //after preprocessing: record the diameter of each quadtree block?
    }
}
