package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.HybridDOEDPIndex;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;

import java.io.*;
import java.util.HashMap;

public class DiameterLoader {
    HybridDOEDPIndex index;
    File diameterFile;
    HashMap<Integer, Float> quadtreeDiameterMap;
    public DiameterLoader(HybridDOEDPIndex index, File diameterFile){
        this.index=index;
        this.diameterFile=diameterFile;
        quadtreeDiameterMap = new HashMap<>();
    }
    public void load() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(diameterFile));
        String line;
        while((line=reader.readLine())!=null){
            String fields[] = line.split(",");
            int id = Integer.valueOf(fields[0]);
            float diameter = Float.valueOf(fields[1]);
            if(quadtreeDiameterMap.containsKey(id)){
                throw new Exception("Error, this id alreayd exits "+id);
            }
            quadtreeDiameterMap.put(id, diameter);
        }
        reader.close();
        for(int i=0; i<index.getNumOfPartitions();i++){
            Partition p = index.partitions[i];
            for(int j=0; j<p.ConnectedComponents.getConnectedComponentsCount();j++){
                ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(j);
                if(cc.tree!=null){
                    cc.tree.loadDiameter(quadtreeDiameterMap);
                }
            }
        }
        quadtreeDiameterMap = null;
        System.out.println("load preprocessing finished");
    }
}
