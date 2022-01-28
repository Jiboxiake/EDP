package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

//try to make this class completely static so we can just call its method for quadtree class.
public class DistanceOracle {
    public static double e=0.25;
    public static double s=2.0/e;

    public static void setParameter(double error){
        e=error;
        s=2.0/e;
    }

   /* public boolean isWellSeparated(int distance, QuadTree t1, QuadTree t2, Graph g, long id1, long id2, Integer Label) throws ObjectNotFoundException {
        return semi_dijkstra(distance, id1, g, Label, t1)&&semi_dijkstra(distance, id2, g, Label, t2);
    }*/
    /*
    //Here we will implement an optimization.
     */
    public static boolean isWellSeparated(float distance, QuadTree t1, QuadTree t2, PartitionVertex u, PartitionVertex v, HashMap<Integer, PartitionVertex> vertices)throws ObjectNotFoundException{
        double adjusted_d= distance/s;
        return approximate_comparison(adjusted_d, t1, u, vertices)&&approximate_comparison(adjusted_d, t2, v, vertices);
    }
    /*
    check when we reach a distance greater than distance/s, if we have traversed all vertices in a Quadtree block.
     */
    //todo: may need to change VertexID type to int
    public static boolean approximate_comparison(double distance, QuadTree t, PartitionVertex u, HashMap<Integer, PartitionVertex> vertices)throws ObjectNotFoundException{
        //QuadTree copy = t.copy();
        HashSet<Integer> copy = t.copy();
        PriorityQueue<DistanceFromSource> pq = new PriorityQueue<>();
        DistanceFromSource uDist = new DistanceFromSource();
        uDist.VertexID=u.getId();
        uDist.Distance=0;
        Map<Integer, DistanceFromSource> distMap = new HashMap<Integer, DistanceFromSource>();
        distMap.put(u.getId(),uDist);
        DistanceFromSource toDist = null;
        pq.add(uDist);
        while(!pq.isEmpty()){
            uDist=pq.poll();
            PartitionVertex v=vertices.get((int)uDist.VertexID);
           if(copy.contains((int)uDist.VertexID)){
               copy.remove((int)uDist.VertexID);
           }
            if(uDist.Distance>distance){
                return copy.isEmpty();
            }
            for(PartitionEdge pe: v.getOutEdges()){
                PartitionVertex to = pe.getTo();
                toDist = distMap.get(to.getId());
                if(toDist==null){
                    toDist = new DistanceFromSource();
                    toDist.VertexID = to.getId();
                    toDist.Distance = Float.POSITIVE_INFINITY;
                    distMap.put((int)toDist.VertexID, toDist);
                }
                if(toDist.Distance > uDist.Distance + pe.getWeight()){
                    toDist.Distance= pe.getWeight()+uDist.Distance;
                    pq.remove(toDist);
                    pq.add(toDist);
                }
            }
        }
        //traverse all vertices in the block and the max distance is still leq distance, we know they are well separated then
        return true;
    }





}
