package dedp.indexes.edgedisjoint;

import dedp.DistanceOracles.MortonCode;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.SearchKey;
import dedp.exceptions.ObjectNotFoundException;

import java.util.HashMap;
/*
we try to create CC class to hold more data
 */
public class ConnectedComponent {
    public int ID;
    public int PartitionID;
    public HashMap<MortonCode, PartitionVertex> vertices;
    public HashMap<Integer, PartitionEdge> edges;
    public HashMap<SearchKey, Integer> DO;
    //public int timeStamp;
    public QuadTree tree;

    public ConnectedComponent(int id, int PartitionID, HashMap<MortonCode, PartitionVertex> vertices, HashMap<Integer, PartitionEdge> edges){
        this.vertices=vertices;
        this.edges=edges;
        this.ID=id;
        this.PartitionID=PartitionID;
        tree=new QuadTree(this.vertices);
       // this.timeStamp=timeStamp;
        DO=new HashMap<>();
    }

    public void insert(PartitionVertex v){
        vertices.put(v.morton(), v);
        DO.clear();
    }

    public void addEntry(PartitionVertex u, PartitionVertex v, int distance) throws ObjectNotFoundException {
        if(!tree.contain(u)){
            throw new ObjectNotFoundException("vertex: "+u.getId()+" not exist in connected component "+ID+" in partition "+PartitionID);
        }
        if(!tree.contain(v)){
            throw new ObjectNotFoundException("vertex: "+v.getId()+" not exist in connected component "+ID+" in partition "+PartitionID);
        }
        QuadTree forU=tree, forV=tree;
        while(true){
            forU=forU.containingBlock(u);
            forV=forV.containingBlock(v);

        }
    }
    /*
    returning the approximate distance between a source and a destination
    returning -1 if DO entry doesn't exist
     */
    public static int lookUp(){
        return -1;
    }
}
