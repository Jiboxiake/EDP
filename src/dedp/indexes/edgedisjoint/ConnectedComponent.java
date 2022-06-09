package dedp.indexes.edgedisjoint;

import dedp.DistanceOracles.*;
import dedp.DistanceOracles.Analytical.CCInfoCOntainer;
import dedp.DistanceOracles.Analytical.ConnectedComponentAnalyzer;
import dedp.exceptions.ObjectNotFoundException;

import java.io.*;
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
    public DistanceOracleBridgeThread[] DOBridgeThreads;
    public DistanceOracleDirectThread[] DODirectThreads;
    public int numDOBridgeThreads;
    public int numDODirectThreads;
    public boolean hasBridgeWorker;
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
        if(bridgeVertices!=null)
            Global.total_bridge_vertices+=bridgeVertices.size();
        this.vertexToBridgeEdges = new HashMap<Integer, BridgeEdgesEntry>();
        this.vertices=vertices;
        this.edges=edges;
        this.ID=id;
        //load balancing and initialize DO threads
        Global.total_partition_vertex+=this.vertices.size();
        Global.total_partition_edge+=this.edges.size();
        this.numDOBridgeThreads = vertices.size()/DistanceOracle.balancer;
        this.numDODirectThreads = numDOBridgeThreads/4;
        if(this.numDOBridgeThreads==0){
            hasBridgeWorker=false;
        }
        else{
            hasBridgeWorker=true;
        }
        /*if(this.numDOBridgeThreads==0){
            this.numDOBridgeThreads=1;
        }
        if( this.numDODirectThreads==0){
            this.numDODirectThreads=1;
        }*/
    /*    if(hasBridgeWorker) {
            DOBridgeThreads = new DistanceOracleBridgeThread[this.numDOBridgeThreads];
            for (int i = 0; i < this.numDOBridgeThreads; i++) {
                DOBridgeThreads[i] = new DistanceOracleBridgeThread();
                DOBridgeThreads[i].setCC(this);
                Global.add_total_do_threads();
                int c = Global.total_do_threads;
                DOBridgeThreads[i].start();
            }
        }*/
      /*  DODirectThreads = new DistanceOracleDirectThread[numDODirectThreads];
        for(int i=0; i<this.numDODirectThreads; i++){
            DODirectThreads[i] = new DistanceOracleDirectThread();
            DODirectThreads[i].setParameter(this);
            Global.add_total_do_threads();
            int c = Global.total_do_threads;
            DODirectThreads[i].start();
        }*/
        //this.cache=new LRU(5);
        this.partition=partition;
        tree=new QuadTree(this.vertices);
        // this.timeStamp=timeStamp;
        DO=new HashMap<>();
        CCInfoCOntainer o = new CCInfoCOntainer(this.ID, this.partition.Label,this.vertices.size(),this.edges.size());
        ConnectedComponentAnalyzer.insert(o);
    }

    public void print(){
        System.out.println("This is connected component: "+ID);
        System.out.println("This has "+vertices.size()+ " vertices.");
        System.out.println("This has "+edges.size()+" edges.");
        System.out.println("This has bridge vertices "+bridgeVertices.size()+" vertices");
    }

    public PartitionVertex getVertex(int vertexID) throws ObjectNotFoundException {
        if(vertices.containsKey(vertexID)){
            return vertices.get(vertexID);
        }else{
            throw new ObjectNotFoundException("Vertex "+vertexID+" not found");
        }
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
                    SearchKey key = new SearchKey(forU.getMC(), forV.getMC());
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
                    SearchKey key = new SearchKey(forU.getMC(), forV.getMC());
                    entry.key=key;
                    Global.addWSP();
                    Global.addBridge_do_count();
                    return entry;
                }
                Global.addNotWellSeparated();
            }
        }
        finally{

        }

    }

    public SearchKey getSearchKey(PartitionVertex u, PartitionVertex v, float distance) throws ObjectNotFoundException {
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
                    SearchKey key = new SearchKey(forU.getMC(), forV.getMC());
                    Global.addWSP();
                    Global.addBridge_do_count();
                    return key;
                }
                Global.addNotWellSeparated();
            }
        }
        finally{

        }
    }

    //for inserting a partial distance oracle
    public void addDO(HashMap<SearchKey, Float> partialDO){
        this.writeLock.lock();
        for(Map.Entry<SearchKey,Float>set:partialDO.entrySet()){
            DO.remove(set.getKey());
            DO.put(set.getKey(),set.getValue());
        }
        this.writeLock.unlock();
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
        //HashMap<Integer, PartitionVertex> potentialBridgeDestinations = new HashMap<>();
        HashSet<Integer> potentialBridgeDestinations = new HashSet<>();
        boolean got=true;
        this.readLock.lock();
        for(Map.Entry<Integer, PartitionVertex>set:bridgeVertices.entrySet()){
            if(source.getId()!=set.getKey()){
                float result= this.noLockLookUp(source, set.getValue());
                if(result<0) {
                    potentialBridgeDestinations.add(set.getKey());
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
        this.readLock.unlock();
        Collections.sort(bridgeList);

        //if DO doesn't contain everything, we must start computation
        if(!got){
            //source.lock.lock();
            //source.numOfBridgeEdgesComputed=bridgeList.size();
            source.thread=new BridgeEdgeThread();
            source.underBridgeComputation=true;
            source.numOfBridgeEdgesComputed=0;
            //source.thread.setParameters(this,source,potentialBridgeDestinations,bridgeList,0);
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
            SearchKey key = new SearchKey(u.morton(), v.morton());
            //todo: only for undirected graph
            // SearchKey reverseKey = new SearchKey(v.mc,u.mc);
            for (int i = 0; i < 33; i++) {
                if (DO.containsKey(key)) {
                    if(DO.get(key)<0){
                        throw new RuntimeException("wrong DO entry got inserted\n");
                    }
                    Global.DO_hit();
                    return DO.get(key);
                }/*else if(DO.containsKey(reverseKey)){
                    Global.DO_hit();
                    return DO.get(reverseKey);
                }*/
                key.shift();
                //reverseKey.shift();
            }
        }catch(RuntimeException e){
            e.printStackTrace();
        }
        finally{
            readLock.unlock();
        }

        return -1;
    }
    //now let's just add locks
    public float noLockLookUp(PartitionVertex u, PartitionVertex v){
        this.readLock.lock();
        try {
            SearchKey key;
            key = new SearchKey(u.morton(), v.morton());
           // key.printBit();
            //todo: only for undirected graph
            // SearchKey reverseKey = new SearchKey(v.mc,u.mc);
            for (int i = 0; i < MortonCode.max_depth; i++) {
                if (DO.containsKey(key)) {
                    if(DO.get(key)<0){
                        throw new RuntimeException("wrong DO entry got inserted\n");
                    }
                    //key.printBit();
                  //  u.mc.printBit();
                  //  v.mc.printBit();
                    Global.DO_hit();
                    //float result = DO.get(key);
                    //System.out.println("got");
                    float result =DO.get(key);
                    this.readLock.unlock();
                    return result;
                }/*else if(DO.containsKey(reverseKey)){
                    Global.DO_hit();
                    return DO.get(reverseKey);
                }*/
                key.shift();
                //key.printBit();
                //reverseKey.shift();
            }
        }catch(RuntimeException e){
            e.printStackTrace();
        }
        if(Global.debug){
            System.out.println("shouldn't happen");
        }
        this.readLock.unlock();
        return -1;
    }

  /*  public SearchKey optimizedSearchKeyGeneration(HashMap<Integer, VertexQueueEntry>distMap, PartitionVertex u, PartitionVertex v, float distance) throws ObjectNotFoundException {
        //use distance map to update each quadtree block's diameter is curr>original.
        //then we only have to check the diameter of the destination's quadtree block. We can also cache it.
        //we can also start with an initial depth, like 2?
        try {
            if (!tree.contain(u)) {
                throw new ObjectNotFoundException("vertex: " + u.getId() + " not exist in connected component " + ID + " in partition " + partition.Label);
            }
            if (!tree.contain(v)) {
                throw new ObjectNotFoundException("vertex: " + v.getId() + " not exist in connected component " + ID + " in partition " + partition.Label);
            }
            //we start searching at an initial depth
            QuadTree forU = tree, forV = tree;
            for(int i=0;i<DistanceOracle.initialDpeth;i++){
                forU = forU.containingBlock(u);
                forV = forV.containingBlock(v);
            }
            while (true) {
                forU = forU.containingBlock(u);
                forV = forV.containingBlock(v);
                assert (forU.getLevel() == forV.getLevel());
                if (DistanceOracle.isWellSeparated(distance, forU, forV, u, v, vertices)||(forU.reachMaxLevel()&&forV.reachMaxLevel())) {
                    SearchKey key = new SearchKey(forU.getMC(), forV.getMC(), forU.getLevel());
                    Global.addWSP();
                    Global.addBridge_do_count();
                    return key;
                }
                if(DistanceOracle.isWellSeparatedOpti(distance,forU,forV,u,v,distMap,this)||(forU.reachMaxLevel()&&forV.reachMaxLevel())){
                    SearchKey key = new SearchKey(forU.getMC(), forV.getMC(), forU.getLevel());
                    Global.addWSP();
                    Global.addBridge_do_count();
                    return key;
                }

                Global.addNotWellSeparated();
            }
        }
        finally{

        }
    }*/
  public SearchKey optimizedSearchKeyGeneration(PartitionVertex u, PartitionVertex v, float distance )throws ObjectNotFoundException{
      try {
          if (!tree.contain(u)) {
              throw new ObjectNotFoundException("vertex: " + u.getId() + " not exist in connected component " + ID + " in partition " + partition.Label);
          }
          if (!tree.contain(v)) {
              throw new ObjectNotFoundException("vertex: " + v.getId() + " not exist in connected component " + ID + " in partition " + partition.Label);
          }
          //we start searching at an initial depth
          QuadTree forU = tree, forV = tree;
          for(int i=0;i<QuadTree.initial_depth;i++){
              forU = forU.containingBlock(u);
              forV = forV.containingBlock(v);
          }
          while (true) {
              if(forU.getLevel() != forV.getLevel()){
                  throw new RuntimeException("error, wrong levels between trees");
              }
        /*      if (DistanceOracle.isWellSeparatedOpti(distance, forU, forV, u, v)||(forU.reachMaxLevel()&&forV.reachMaxLevel())) {
                  SearchKey key = new SearchKey(forU.getMC(), forV.getMC(), forU.getLevel());
                  Global.addWSP();
                  Global.addBridge_do_count();
                  return key;
              }*/
              if(DistanceOracle.isWellSeparatedOpti(distance,forU,forV,u,v)||(forU.reachMaxLevel()&&forV.reachMaxLevel())){
                  SearchKey key = new SearchKey(forU.getMC(), forV.getMC());
                /*  if(v.getId()==5769&&u.getId()==5659){
                      forV.getMC().printBit();
                      forV.getParent().getMC().printBit();
                      v.morton().printBit();

                      forU.getMC().printBit();
                      forU.getParent().getMC().printBit();
                      u.morton().printBit();
                      key.printBit();
                  }*/
                 // key.printBit();
                  Global.addWSP();
                  Global.addBridge_do_count();
                  Global.addLevel(forU.getLevel());
                  return key;
              }
              forU = forU.containingBlock(u);
              forV = forV.containingBlock(v);
              Global.addNotWellSeparated();
          }
      }
      finally{

      }
  }

    /*
    in this method, we either found a hybrid bridge edge list under computation, start a computation or return a full list without needing a computation
     */
    //todo: check the correctness of my fix. Make sure they are correct
    public HybridBridgeEdgeList getBridgeEdgeList(PartitionVertex source){
        ArrayList<PartitionEdge> doList=null;
        ArrayList<PartitionEdge> computedList=null;
        HybridBridgeEdgeList bridgeList = new HybridBridgeEdgeList(source, this);
        source.lock.lock();
        //todo: work on this part for my bridge edge thread
        if(source.underBridgeComputation){
            //source.thread.copyList(doList,computedList);
            doList=source.thread.copyDOList();
            computedList = source.thread.copyComputedList();
            bridgeList.setParameters(doList,computedList);
            source.lock.unlock();
            return bridgeList;
        }else{
            doList = new ArrayList<>();
            computedList = new ArrayList<>();
            boolean got = true;
            //if this cc has not bridge vertices, we just skip any work
            if(this.bridgeVertices==null){
                bridgeList.setParameters(doList,computedList);
                source.underBridgeComputation =false;
                source.allBridgeEdgesComputed=true;
                source.numOfBridgeEdgesComputed = 0;
                return bridgeList;
            }
            this.readLock.lock();
            HashSet<Integer> potentialBridgeDestinations = new HashSet<>();
            //there may be connected components with no bridge vertex
            for(Map.Entry<Integer, PartitionVertex>set:bridgeVertices.entrySet()){
                if(source.getId()!=set.getKey()){
                    float result= this.noLockLookUp(source, set.getValue());
                    if(result<0) {
                        potentialBridgeDestinations.add(set.getKey());
                        got = false;
                    }else{
                        PartitionEdge e = new PartitionEdge();
                        e.setFrom(source);
                        e.setTo(set.getValue());
                        e.setWeight(result);
                        e.setLabel(this.partition.Label);
                        doList.add(e);
                        //bridgeList.add(e);
                    }
                }
            }
            this.readLock.unlock();
            Collections.sort(doList);
            if(!got){
                source.thread=new BridgeEdgeThread();
                source.underBridgeComputation=true;
                source.numOfBridgeEdgesComputed=0;
                if(Global.debug){
                    System.out.println("Should not");
                }
                //Global.list.add(source.thread);
                Global.addThread(source.thread);
                source.thread.setParameters(this, source, potentialBridgeDestinations, doList, computedList, 0);
                source.thread.start();
            }else{
                source.allBridgeEdgesComputed=true;
                source.numOfBridgeEdgesComputed = doList.size();
            }
            bridgeList.setParameters(doList,computedList);
            source.lock.unlock();
            return bridgeList;
        }
    }

    public void computeBridgeDO() throws InterruptedException {
        if(this.bridgeVertices==null||this.bridgeVertices.size()==0){
            return;
        }
        PartitionVertex source = null;
        for(Map.Entry<Integer,PartitionVertex>set: this.bridgeVertices.entrySet()){//this should populate all distance oracles.
            source = set.getValue();
            getBridgeEdgeList(source);
            if(source.thread!=null){
                source.thread.join();
            }
        }
    }
    public void outputDO() throws IOException, InterruptedException {
        String uniqueID = "./DistanceOracle/"+this.partition.Label+"_"+this.ID+".txt";
        FileWriter fileWriter = new FileWriter(uniqueID);
        String result;
        computeBridgeDO();
        for(Map.Entry<SearchKey,Float>set:DO.entrySet()){
            result = set.getKey().mc+","+(int)set.getKey().level+","+set.getValue()+"\n";
            fileWriter.write(result);
        }

        fileWriter.close();
    }
    public void inputDO() throws IOException {
        String uniqueID = "./DistanceOracle/"+this.partition.Label+"_"+this.ID+".txt";
        FileReader reader = new FileReader(uniqueID);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while((line=br.readLine())!=null){
            String[]fields = line.split(",");
            long key = Long.parseLong(fields[0]);
            int level = Integer.parseInt(fields[1]);
            float distance = Float.parseFloat(fields[2]);
            SearchKey sk = new SearchKey(key,level);
            DO.put(sk,distance);
        }
        reader.close();
        br.close();
    }
    public void addSingleDO(SearchKey key, float result){
        this.writeLock.lock();
        if(this.DO.containsKey(key)){
            this.writeLock.unlock();
            return;
        }
        this.DO.put(key,result);
        this.writeLock.unlock();
    }
}