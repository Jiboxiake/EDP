package dedp.indexes.edgedisjoint;

import dedp.DistanceOracles.DistanceOracle;
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
    public Partition partition;
    public HashMap<Integer, PartitionVertex> vertices;
    public HashMap<Integer, PartitionEdge> edges;
    public HashMap<SearchKey, Integer> DO;
    //public int timeStamp;
    public QuadTree tree;

    public ConnectedComponent(int id, Partition partition, HashMap<Integer, PartitionVertex> vertices, HashMap<Integer, PartitionEdge> edges){
        this.vertices=vertices;
        this.edges=edges;
        this.ID=id;
        this.partition=partition;
        tree=new QuadTree(this.vertices);
       // this.timeStamp=timeStamp;
        DO=new HashMap<>();
    }

    public void insert(PartitionVertex v){
        vertices.put(v.getId(), v);
        DO.clear();
    }
    //TODO: may need to change distance type, now assume they are all integers
    public void addEntry(PartitionVertex u, PartitionVertex v, int distance) throws ObjectNotFoundException {
        if(!tree.contain(u)){
            throw new ObjectNotFoundException("vertex: "+u.getId()+" not exist in connected component "+ID+" in partition "+partition.Label);
        }
        if(!tree.contain(v)){
            throw new ObjectNotFoundException("vertex: "+v.getId()+" not exist in connected component "+ID+" in partition "+partition.Label);
        }
        QuadTree forU=tree, forV=tree;
        while(true){
            forU=forU.containingBlock(u);
            forV=forV.containingBlock(v);
            assert(forU.getLevel()==forV.getLevel());
            if(DistanceOracle.isWellSeparated(distance, forU, forV, u, v)){
                SearchKey key = new SearchKey(forU.getMC(), forV.getMC(), forU.getLevel());
                DO.put(key, distance);
                return;
            }
        }
    }
    /*
    returning the approximate distance between a source and a destination
    returning -1 if DO entry doesn't exist
     */
    //todo: may need to change this to long or double
    public int lookUp(PartitionVertex u, PartitionVertex v){
        SearchKey key = new SearchKey(u.mc, v.mc);
        for(int i=0; i<33; i++){
            if(DO.containsKey(key)){
                assert(DO.get(key)>0);
                return DO.get(key);
            }
            key.shift();
        }
        return -1;
    }
}
