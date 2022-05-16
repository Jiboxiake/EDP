package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

//try to make this class completely static so we can just call its method for quadtree class.
public class DistanceOracle {
    public static double e=0.01;
    public static double s=2.0/e;
    public static int initialDpeth=2;
    public static int balancer = 2000;

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
        HashSet<Integer> copy = new HashSet<>();
        t.copy(copy);
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
    //maybe we still want 1 thread per cc so there will be less wasted work
    //or dedicated DO computation thread, in a producer consumer model
    public static float getQuadTreeDiameter(PartitionVertex source, ConnectedComponent cc, QuadTree t) throws ObjectNotFoundException {
        float maxDistance = t.getDiameter();
        if(maxDistance>0){
            return maxDistance;
        }
        PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
        PartitionVertex u = null;
        DistanceFromSource uDist = new DistanceFromSource();
        uDist.VertexID = source.getId();
        uDist.Distance = 0;
        q.add(uDist);
        Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
        distMap.put((long)source.getId(), uDist);
        DistanceFromSource toDist = null;

        HashSet<Integer> allVer = new HashSet<>();
        t.copy(allVer);
        while(!q.isEmpty())
        {
            uDist = q.poll();
            //return until we traverse all vertices in this quadtree block.
            if(allVer.contains((int)uDist.VertexID)){
                //we only care about distances in this block
                if(maxDistance<uDist.Distance){
                    maxDistance=uDist.Distance;
                }
                allVer.remove((int)uDist.VertexID);
                if(allVer.isEmpty()){
                    t.setDiameter(maxDistance);
                    //for debug
                   // System.out.println("found diameter");
                    q=null;
                    distMap=null;
                    return maxDistance;
                }
            }
            u = cc.getVertex((int)uDist.VertexID);
            for(PartitionEdge e: u.getOutEdges()){
                PartitionVertex to = e.getTo();
                toDist = distMap.get((long)to.getId());
                if(toDist==null){
                    toDist = new DistanceFromSource();
                    toDist.VertexID = to.getId();
                    toDist.Distance = uDist.Distance + e.getWeight();
                    distMap.put((long)toDist.VertexID, toDist);
                    q.add(toDist);
                }
                if(toDist.Distance > uDist.Distance + e.getWeight())
                {
                    toDist.Distance = uDist.Distance + e.getWeight();
                    q.remove(toDist); //remove if it exists
                    q.add(toDist);
                }
            }
        }
        throw new ObjectNotFoundException("quadtree block contains extra vertices");
    }

    public static float getQuadTreeDiameterWithDistMap(HashMap<Integer, VertexQueueEntry>disMap, QuadTree t){
        float maxDistance = -1;
        HashSet<Integer>vertices = new HashSet<>();
        t.copy(vertices);
        for(Integer id:vertices){
            if(disMap.get(id).distance>maxDistance){
                maxDistance=disMap.get(id).distance;
            }
        }
        t.setDiameter(maxDistance);
        return maxDistance;
    }

    /*public static boolean isWellSeparatedOpti(float distance, QuadTree t1, QuadTree t2, PartitionVertex u, PartitionVertex v, HashMap<Integer, VertexQueueEntry>distMap, ConnectedComponent cc) throws ObjectNotFoundException {
        float d1 = getQuadTreeDiameterWithDistMap(distMap,t1);
        float d2 = getQuadTreeDiameter(v,cc,t2);
        double adjusted_d= distance/s;
        if(adjusted_d>d1&&adjusted_d>d2){
            return true;
        }
        return false;
    }*/
    public static boolean isWellSeparatedOpti(float distance,QuadTree t1, QuadTree t2, PartitionVertex u, PartitionVertex v){
        if(t1.getDiameter()<0){
            throw new RuntimeException("error, a quadtree block containing a bridge vertex has negative diameter "+t1.getDiameter());
        }if(t2.getDiameter()<0){
            throw new RuntimeException("error, a quadtree block containing a bridge vertex has negative diameter "+t2.getDiameter());
        }

        return ((distance/(float)s>t1.getDiameter())&&(distance/(float)s>t2.getDiameter()));
    }

}
