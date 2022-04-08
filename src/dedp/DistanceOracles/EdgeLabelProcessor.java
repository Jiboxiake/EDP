package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;

import java.util.HashMap;

public class EdgeLabelProcessor {
    public static HashMap<Integer, Integer> rawLabelToEDPLabel=new HashMap<>();
    public static HashMap<Integer, Integer> EDPLabelToRawLabel=new HashMap<>();
    public static int key=0;

    public static void insert(int label){
        if(rawLabelToEDPLabel.containsKey(label)){
            return;
        }
        rawLabelToEDPLabel.put(label, key);
        EDPLabelToRawLabel.put(key,label);
        key++;
    }
    public static int translate(int label) throws ObjectNotFoundException {
        if(!rawLabelToEDPLabel.containsKey(label)){
            throw new ObjectNotFoundException("label "+label+" not found\n");
        }
        return rawLabelToEDPLabel.get(label);
    }
}
