package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.HashMap;
import java.util.Map;

public class QuadForest {
   public HashMap<Integer, QuadTree> forest;

   public QuadForest(){
       forest=new HashMap<>();
   }
   public QuadForest(HashMap<Integer, HashMap<Integer, PartitionVertex>> quadTreeSet){
       forest=new HashMap<>();
       for(Map.Entry<Integer, HashMap<Integer, PartitionVertex>> e: quadTreeSet.entrySet()){
           QuadTree tree = new QuadTree(e.getValue());
           forest.put(e.getKey(), tree);
       }
   }

   public void insert(Integer id, HashMap<Integer, PartitionVertex>vertices) throws Exception {
       if(forest.containsKey(id)){
           throw new Exception("quadtree "+id+" already exists");
       }
       QuadTree tree = new QuadTree(vertices);
        forest.put(id, tree);
   }


}
