package dedp.indexes.edgedisjoint;

import dedp.DistanceOracles.*;
import dedp.exceptions.ObjectNotFoundException;

import java.util.*;
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
   // private LRU cache;
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
        //this.cache=new LRU(5);
        this.partition=partition;
        tree=new QuadTree(this.vertices);
       // this.timeStamp=timeStamp;
        DO=new HashMap<>();
    }

    public void print(){
        System.out.println("This is connected component: "+ID);
        System.out.println("This has "+vertices.size()+ " vertices.");
        System.out.println("This has "+edges.size()+" edges.");
        System.out.println("This has bridge vertices "+bridgeVertices.size()+" vertices");
    }

    //todo: destroy DO and bridge edge thread when updating the CC
    public void insert(PartitionVertex v){
        vertices.put(v.getId(), v);
        DO.clear();
        vertexToBridgeEdges.clear();
    }
    //TODO: think about how locks may influence the performance.
    public void addDOEntry(PartitionVertex u, PartitionVertex v, float distance) throws ObjectNotFoundException {

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
                    DO.remove(key);
                    DO.put(key, distance);
                    writeLock.unlock();
                    Global.addWSP();
                    return;
                }
                Global.addNotWellSeparated();
            }
        }
        finally{

        }
    }
    //for optimized and parallel DO insertion, no lock
   public DOEntry getEntry(PartitionVertex u, PartitionVertex v, float distance) throws ObjectNotFoundException {
        DOEntry entry = new DOEntry(null, distance);
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
                   entry.key=key;
                   Global.addWSP();
                   return entry;
               }
               Global.addNotWellSeparated();
           }
       }
       finally{

       }

   }
    //try to insert as efficiently as possible
   public void addEntryList(ArrayList<DOEntry> entryList){
       writeLock.lock();
        for(int i=0; i<entryList.size();i++){
            DOEntry entry = entryList.get(i);
            DO.remove(entry.key);
            DO.put(entry.key, entry.distance);
        }
       writeLock.unlock();
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

    public boolean checkBridgeDO(PartitionVertex source, ArrayList<PartitionEdge>bridgeList) throws ObjectNotFoundException {
        if(!vertices.containsKey(source.getId())){
            throw new ObjectNotFoundException("vertex "+source.getId()+" not found in CC "+this.ID);
        }
        HashMap<Integer, PartitionVertex> potentialBridgeDestinations = new HashMap<>();
        boolean got=true;
        for(Map.Entry<Integer, PartitionVertex>set:bridgeVertices.entrySet()){
            if(source.getId()!=set.getKey()){
          float result= this.lookUp(source, set.getValue());
          if(result<0) {
              potentialBridgeDestinations.put(set.getKey(), set.getValue());
              got = false;
          }else{
              PartitionEdge e = new PartitionEdge();
              e.setFrom(source);
              e.setTo(set.getValue());
              e.setWeight(result);
              e.setLabel(this.partition.Label);
              bridgeList.add(e);
          }
            }
        }
        Collections.sort(bridgeList);

        //if DO doesn't contain everything, we must start computation
        if(!got){
            //source.lock.lock();
            //source.numOfBridgeEdgesComputed=bridgeList.size();
            source.thread=new BridgeEdgeThread();
            source.underBridgeComputation=true;
            source.numOfBridgeEdgesComputed=0;
            source.thread.setParameters(this,source,potentialBridgeDestinations,bridgeList,0);
            source.thread.start();
            //System.out.println("execution starts");
           // source.lock.unlock();
        }else{
          //  source.lock.lock();
            source.allBridgeEdgesComputed=true;
            source.numOfBridgeEdgesComputed=bridgeList.size();
          //  source.lock.unlock();
        }
        //Collections.sort(bridgeList);
        return got;
    }


    /*
    returning the approximate distance between a source and a destination
    returning -1 if DO entry doesn't exist
     */
    public float lookUp(PartitionVertex u, PartitionVertex v){
        readLock.lock();
        try {
            SearchKey key = new SearchKey(u.mc, v.mc);
            //todo: only for undirected graph
           // SearchKey reverseKey = new SearchKey(v.mc,u.mc);
            for (int i = 0; i < 33; i++) {
                if (DO.containsKey(key)) {
                    assert (DO.get(key) > 0);
                    Global.DO_hit();
                    return DO.get(key);
                }/*else if(DO.containsKey(reverseKey)){
                    Global.DO_hit();
                    return DO.get(reverseKey);
                }*/
                key.shift();
                //reverseKey.shift();
            }
        }finally{
            readLock.unlock();
        }

            return -1;


    }
}
