package dedp.indexes.edgedisjoint;

import dedp.DistanceOracles.DistanceOracle;
import dedp.DistanceOracles.MortonCode;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.SearchKey;
import dedp.exceptions.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
we try to create CC class to hold more data
 */
public class ConnectedComponent {
    public int ID;
    public Partition partition;
    public HashMap<Integer, PartitionVertex> vertices;
    public HashMap<Integer, PartitionVertex> bridgeVertices;
    public HashMap<Integer, PartitionEdge> edges;
    public HashMap<SearchKey, Float> DO;
    protected HashMap<Integer, BridgeEdgesEntry> vertexToBridgeEdges; //forward
    //public int timeStamp;
    public QuadTree tree;
    //the lock for read and write of distance oracle
    private final ReadWriteLock readWriteLock
            = new ReentrantReadWriteLock();
    private final Lock writeLock
            = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock bridgeReadLock =readWriteLock.readLock();
    private final Lock bridgeWriteLock=readWriteLock.writeLock();

    public ConnectedComponent(int id, Partition partition, HashMap<Integer, PartitionVertex> vertices, HashMap<Integer, PartitionEdge> edges, HashMap<Integer, PartitionVertex> bridgeVertices){
        this.bridgeVertices=bridgeVertices;
        this.vertexToBridgeEdges = new HashMap<Integer, BridgeEdgesEntry>();
        this.vertices=vertices;
        this.edges=edges;
        this.ID=id;
        this.partition=partition;
        tree=new QuadTree(this.vertices);
       // this.timeStamp=timeStamp;
        DO=new HashMap<>();
    }

    public void print(){
        System.out.println("This is connected component: "+ID);
        System.out.println("This has "+vertices.size()+ " vertices.");
        System.out.println("This has "+edges.size()+" edges.");
    }

    //todo: destroy DO and bridge edge thread when updating the CC
    public void insert(PartitionVertex v){
        vertices.put(v.getId(), v);
        DO.clear();
        vertexToBridgeEdges.clear();
    }
    //TODO: think about how locks may influence the performance.
    public void addEntry(PartitionVertex u, PartitionVertex v, float distance) throws ObjectNotFoundException {

        try {
            if (!tree.contain(u)) {
                throw new ObjectNotFoundException("vertex: " + u.getId() + " not exist in connected component " + ID + " in partition " + partition.Label);
            }
            if (!tree.contain(v)) {
                throw new ObjectNotFoundException("vertex: " + v.getId() + " not exist in connected component " + ID + " in partition " + partition.Label);
            }
            QuadTree forU = tree, forV = tree;
            while (true) {
                forU = forU.containingBlock(u);
                forV = forV.containingBlock(v);
                assert (forU.getLevel() == forV.getLevel());
                if (DistanceOracle.isWellSeparated(distance, forU, forV, u, v, vertices)||(forU.reachMaxLevel()&&forV.reachMaxLevel())) {
                    SearchKey key = new SearchKey(forU.getMC(), forV.getMC(), forU.getLevel());
                    writeLock.lock();
                    DO.put(key, distance);
                    writeLock.unlock();
                    return;
                }
            }
        }
        finally{

        }
    }

    public void addBridgeEntry(int from, PartitionEdge e){
        this.bridgeWriteLock.lock();
        this.vertexToBridgeEdges.get(from).BridgeEdges.add(e);
        this.bridgeWriteLock.unlock();
    }
    public BridgeEdgesEntry createNewEntry(int from){
        this.bridgeWriteLock.lock();
        BridgeEdgesEntry entry = new BridgeEdgesEntry();
        ArrayList<PartitionEdge> toBridgeEdges = new ArrayList<PartitionEdge>(bridgeVerticesSize());
        entry.BridgeEdges=toBridgeEdges;
        vertexToBridgeEdges.put(from, entry);
        this.bridgeWriteLock.unlock();
        return entry;
    }
    public BridgeEdgesEntry getBridgeEdgeEntry(int from){
        this.bridgeReadLock.lock();
        BridgeEdgesEntry entry = vertexToBridgeEdges.get(from);
        this.bridgeReadLock.unlock();
        return entry;
    }

    public int bridgeVerticesSize(){
        if(this.bridgeVertices==null){
            return 0;
        }
        return this.bridgeVertices.size();
    }

    /*
    returning the approximate distance between a source and a destination
    returning -1 if DO entry doesn't exist
     */
    //todo: may need to change this to long or double
    public float lookUp(PartitionVertex u, PartitionVertex v){
        readLock.lock();
        try {
            SearchKey key = new SearchKey(u.mc, v.mc);
            for (int i = 0; i < 33; i++) {
                if (DO.containsKey(key)) {
                    assert (DO.get(key) > 0);
                    return DO.get(key);
                }
                key.shift();
            }
        }finally{
            readLock.unlock();
        }
            return -1;


    }
}
