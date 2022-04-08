package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.QuadTree;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.DistanceFromSource;

import java.util.*;

public class DiameterQueryEntry {
    class Container{
        QuadTree tree;
        HashSet<Integer> vertices;
        float max_distance;
        Container(QuadTree tree){
            this.tree = tree;
            this.vertices = new HashSet<>();
            tree.copy(this.vertices);
            max_distance=-1;
        }
        boolean check(int id, float distance){
            if(vertices.contains(id)){
                vertices.remove(id);
                max_distance = distance >max_distance ? distance : max_distance;
                if(vertices.isEmpty()){
                    //System.out.println("block is set");//for debug
                    tree.setDiameter(max_distance);
                }
                return true;
            }
            return false;
        }

        boolean isSet(){
            return vertices.isEmpty();
        }
    }
    public ConnectedComponent cc;
    public PartitionVertex source;//a source bridge vertex
    Container[] containers = new Container[QuadTree.max_depth+1];
    public void computation(){
        QuadTree forU = cc.tree;
        for(int i=0; i<=QuadTree.max_depth; i++){
            if(forU==null){
                break;
            }
            Container c = new Container(forU);
            forU = forU.containingBlock(source);
            containers[i]=c;
        }
        try {
            getQuadTreeDiameter();
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        //ok now we have quadtree at all levels

    }

    public void getQuadTreeDiameter() throws ObjectNotFoundException {
        float max_distance = -1;
        PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
        PartitionVertex u = null;
        DistanceFromSource uDist = new DistanceFromSource();
        uDist.VertexID = source.getId();
        uDist.Distance = 0;
        q.add(uDist);
        Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
        distMap.put((long)source.getId(), uDist);
        DistanceFromSource toDist = null;
        //todo: check if this algorithm is correct
        while(!q.isEmpty())
        {
            uDist = q.poll();
            boolean allComputed = true;
            //return until we traverse all vertices in this quadtree block.
            for(int i = QuadTree.initial_depth; i<=QuadTree.max_depth;i++){
                if(containers[i]==null){
                    break;
                }
                Container ct = containers[i];
                if(!ct.isSet()){
                    ct.check((int)uDist.VertexID,uDist.Distance);
                    if(!ct.isSet()){
                        allComputed=false;
                    }
                }//else do nothing, we don't care about the ones settled.
            }
            if(allComputed){
                return;
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
    }


}
