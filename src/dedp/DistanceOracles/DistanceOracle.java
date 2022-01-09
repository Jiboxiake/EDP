package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.*;

import java.util.HashMap;
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
    /*
    //Here we will implement an optimization.
     */
    public boolean isWellSeparated(int distance, QuadTree t1, QuadTree t2, Graph g, long id1, long id2, Integer Label) throws ObjectNotFoundException {
        return semi_dijkstra(distance, id1, g, Label, t1)&&semi_dijkstra(distance, id2, g, Label, t2);
    }
    public boolean isWellSeparated(int distance, QuadTree t1, QuadTree t2, PartitionVertex u, PartitionVertex v)throws ObjectNotFoundException{

        return false;
    }
    /*
    check when we reach a distance greater than distance/s, if we have traversed all vertices in a Quadtree block.
     */
    public boolean approximate_comparison(double distance, QuadTree t, PartitionVertex u)throws ObjectNotFoundException{
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
           // PartitionVertex u=t.getVertex()
        }
        return false;
    }


    //todo: want to only do this on a partition subgraph, not a whole graph
    public boolean semi_dijkstra(int distance, long source, Graph graph, Integer Label, QuadTree t1)throws ObjectNotFoundException {
        distance = distance/(int)s;
        SPResult result = new SPResult();
        result.Distance = -1;
        result.NumberOfExploredEdges = 0;
        result.NumberOfExploredNodes = 0;
        //intialize single-source
        PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
        Vertex u = null;
        DistanceFromSource uDist = new DistanceFromSource();
        uDist.VertexID = source;
        uDist.Distance = 0;
        q.add(uDist);
        Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
        distMap.put(source, uDist);
        DistanceFromSource toDist = null;
        while(!q.isEmpty())
        {
            result.NumberOfExploredNodes++;
            uDist = q.poll();
            u = graph.getVertex(uDist.VertexID);
            //remove the most recent vertex from the quadtree
            MortonCode mc = u.mc;
            t1.removal(mc);
            if(uDist.Distance>distance)
            {
               return t1.isEmpty();
            }
            for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
            {
                if(Label==(e.getLabel()))
                {
                    Vertex to = e.getTo();
                    result.NumberOfExploredEdges++;
                    //get the distance of to node
                    toDist = distMap.get(to.getID());
                    if(toDist == null)
                    {
                        toDist = new DistanceFromSource();
                        toDist.VertexID = to.getID();
                        toDist.Distance = Float.POSITIVE_INFINITY;
                        distMap.put(toDist.VertexID, toDist);
                    }
                    if(toDist.Distance > uDist.Distance + e.getWeight())
                    {
                        toDist.Distance = uDist.Distance + e.getWeight();
                        q.remove(toDist); //remove if it exists
                        q.add(toDist);
                    }
                }
            }
        }
        return false;
    }

}
