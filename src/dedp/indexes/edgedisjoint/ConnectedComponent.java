package dedp.indexes.edgedisjoint;

import com.google.flatbuffers.FlatBufferBuilder;
import dedp.DistanceOracles.*;
import dedp.DistanceOracles.Analytical.CCInfoCOntainer;
import dedp.DistanceOracles.Analytical.ConnectedComponentAnalyzer;
import dedp.DistanceOracles.FlatBuffer.Oracle;
import dedp.DistanceOracles.FlatBuffer.Wsp;
import dedp.algorithms.Dijkstra;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.exceptions.ObjectNotFoundException;
import dedp.structures.SPResult;

import java.io.*;
import java.nio.file.Files;
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
    public void outputBridgeDO() throws IOException, InterruptedException {
        String uniqueID = "./DistanceOracles/"+this.partition.Label+"_"+this.ID+".txt";
        FileWriter fileWriter = new FileWriter(uniqueID);
        String result;
        computeBridgeDO();
        for(Map.Entry<SearchKey,Float>set:DO.entrySet()){
            result = set.getKey().mc+","+(int)set.getKey().level+","+set.getValue()+"\n";
            fileWriter.write(result);
        }
        fileWriter.close();
    }

    public void outputDO() throws IOException, InterruptedException {
        String uniqueID = "./DistanceOracles/"+this.partition.Label+"_"+this.ID+".txt";
        FileWriter fileWriter = new FileWriter(uniqueID);
        String result;
        for(Map.Entry<SearchKey,Float>set:DO.entrySet()){
            result = set.getKey().mc+","+(int)set.getKey().level+","+set.getValue()+"\n";
            fileWriter.write(result);
        }
        fileWriter.close();
    }

    public void inputDO() throws IOException {
        String uniqueID = "./DistanceOracles/"+this.partition.Label+"_"+this.ID+".txt";
        FileReader reader = new FileReader(uniqueID);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while((line=br.readLine())!=null){
            String[]fields = line.split(",");
            long key = Long.parseLong(fields[0]);
            int level = Integer.parseInt(fields[1]);
            float distance = Float.parseFloat(fields[2]);
            SearchKey sk = new SearchKey(key, (short) level);
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
    public boolean testDO(int count){
        boolean result = true;
        for(Map.Entry<Integer,PartitionVertex>set:vertices.entrySet()){
            PartitionVertex source =set.getValue();
            for(Map.Entry<Integer,PartitionVertex>dset:vertices.entrySet()){
                PartitionVertex destination =dset.getValue();
                if(source.getId()==destination.getId()){
                    continue;
                }
                result&=WSPDCheck(source,destination);
            }
        }
        return result;
    }

    public boolean WSPDCheck(PartitionVertex source, PartitionVertex destination){
        boolean flag = true;
        SearchKey key = new SearchKey(source.morton(),destination.morton());
        int count=0;
        for(int i=0; i<16;i++){
            if(DO.containsKey(key)){
                count++;
            }
            key.shift();
        }
        if(count!=1){
            Global.WSPD_Fail++;
            flag = false;
        }else{
            Global.WSPD_Pass++;
            flag = true;
        }
        return flag;
    }

    public boolean DoQualityTest(int count) throws ObjectNotFoundException {
        if(count>this.vertices.size()){
            count = this.vertices.size();
        }
        boolean result = true;
        Random generator = new Random();
        Object[] values = vertices.values().toArray();
        for(int i=0; i<count; i++){
            PartitionVertex source =(PartitionVertex) values[generator.nextInt(values.length)];
            PartitionVertex destination =(PartitionVertex) values[generator.nextInt(values.length)];
            if(source.getId()==destination.getId()){
                i--;
                continue;
            }
            SPResult sr = Dijkstra.shortestDistance(this.partition,source.getId(),destination.getId());//todo: implement per partition version or per cc version of bidirectional dij.
            float doresult = this.noLockLookUp(source,destination);
            double error = (Math.abs(sr.Distance-doresult))/sr.Distance;
            if(error>Global.maxError){
                Global.maxError = error;
            }
            if(error>0.25){
                System.out.println(source.getId()+" "+destination.getId()+" dij result "+sr.Distance+" do result "+doresult+" Error: "+error);
                Global.addBadDOResult();
                result = false;
            }
        }
        return result;
    }

    public boolean completeDOQualityTest() throws ObjectNotFoundException {
        boolean result = true;
        Random generator = new Random();
        Object[] values = vertices.values().toArray();
        for(Map.Entry<Integer, PartitionVertex>sset:vertices.entrySet()){
            PartitionVertex source =sset.getValue();
            for(Map.Entry<Integer, PartitionVertex>dset:vertices.entrySet()){
                PartitionVertex destination =dset.getValue();
                if(source.getId()==destination.getId()){
                    continue;
                }
                SPResult sr = Dijkstra.shortestDistance(this.partition,source.getId(),destination.getId());//todo: implement per partition version or per cc version of bidirectional dij.
                float doresult = this.noLockLookUp(source,destination);
                double error = (Math.abs(sr.Distance-doresult))/sr.Distance;
                if(error>Global.maxError){
                    Global.maxError = error;
                }
                if(error>0.25){
                    System.out.println(source.getId()+" "+destination.getId()+" dij result "+sr.Distance+" do result "+doresult+" Error: "+error);
                    Global.addBadDOResult();
                    result = false;
                }
            }
        }
        return result;
    }
    public void printDO(){
        for(Map.Entry<SearchKey, Float>set:DO.entrySet()){
            set.getKey().printBit();
            System.out.println(set.getValue());
        }
    }
    public boolean WSPD_Reverse_Test(){//ok it seems like this test also passed
        HashMap<SearchKey,Integer> doChecker = new HashMap<>();
        //initialize the checker
        for(Map.Entry<SearchKey,Float>set:DO.entrySet()){
            doChecker.put(set.getKey(),0);
        }
        for(Map.Entry<Integer, PartitionVertex>set:vertices.entrySet()){
            for(Map.Entry<Integer,PartitionVertex>dset:vertices.entrySet()){
                if(set.getValue().getId()== dset.getValue().getId()){
                    continue;
                }
                PartitionVertex source = set.getValue();
                PartitionVertex destination = dset.getValue();
                SearchKey key = new SearchKey(source.morton(),destination.morton());
                for(int i=0;i<16; i++){
                    if(DO.containsKey(key)){
                        int count = doChecker.get(key);
                        count++;
                        doChecker.put(key,count);
                    }
                    key.shift();
                }
            }
        }
        for(Map.Entry<SearchKey, Integer>set:doChecker.entrySet()){
            int result = set.getValue();
            if(result<=0){
                System.out.print("searchkey ");
                set.getKey().printBit();
                System.out.println("Has value "+set.getValue());
                return false;
            }
        }
        return true;
    }
    public void serialize() throws IOException {
        if(this.DO.size()==0){
            return;
        }
        //first read in DO
        this.inputDO();
        //now let's output do
        FlatBufferBuilder builder = new FlatBufferBuilder(this.vertices.size()*3);
       // Vector<Integer> wsp_vector = new Vector<Integer>();
        int offset=-1;
        //now let's play with each DO entry
        int[]DOs = new int[DO.size()];
        int index=0;
        //Oracle.startWspsVector(builder,DO.size());
        for(Map.Entry<SearchKey,Float>set:DO.entrySet()){
            SearchKey key = set.getKey();
            float dist = set.getValue();
            long code = key.mc;
            short level = key.level;
            System.out.println(""+code+","+level+","+dist);
           DOs[index]= Wsp.createWsp(builder,code,(short)level,dist);
            index++;
        }
        int dos = builder.createVectorOfTables(DOs);
        int oracle = Oracle.createOracle(builder,dos);
        builder.finish(oracle);
        readFlatBuffer(builder);
    }
    public void readFlatBuffer( FlatBufferBuilder builder){
        byte[] bytes = builder.sizedByteArray();
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(bytes);
        Oracle oracle = Oracle.getRootAsOracle(buf);
        Wsp.Vector vector = oracle.wspsVector();
        for(int i=0; i<vector.length();i++){
            Wsp wsp = vector.get(i);
            System.out.println(wsp.code()+","+wsp.level()+","+wsp.distance());
        }
    }
    public void inputDOFlatBuffer() throws IOException {
        File dir = new File("./DOResult/");
        int pID = this.partition.Label;
        int ccID = this.ID;
        File[] foundFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(String.valueOf(pID)+"_"+String.valueOf(ccID));
            }
        });
        for(int i=0; i<foundFiles.length;i++){
            //byte[] fileContent = Files.readAllBytes(foundFiles[i].toPath());
            try (FileInputStream fis = new FileInputStream(foundFiles[i].toPath().toString())) {
                byte[]bytes = fis.readAllBytes();
                parseFlatBufferToDO(bytes);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("Load finish");
    }
    public void parseFlatBufferToDO( byte[] bytes){
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(bytes);
        Oracle oracle = Oracle.getRootAsOracle(buf);
        Wsp.Vector vector = oracle.wspsVector();
        for(int i=0; i<vector.length();i++){
            Wsp wsp = vector.get(i);
            SearchKey key = new SearchKey(wsp.code(),wsp.level());
            float distance = wsp.distance();
            DO.put(key,distance);
            //System.out.println(wsp.code()+","+wsp.level()+","+wsp.distance());
        }

    }
    public boolean checkDOFile(){
        String uniqueID = "./DistanceOracles/"+this.partition.Label+"_"+this.ID+".txt";
        File f = new File(uniqueID);
        if(this.vertices!=null&&this.vertices.size()>1){
            if(f.exists()){
                return true;
            }else{
                return false;
            }
        }else{
            if(f.exists() ){
                return false;
            }else{
                return true;
            }
        }
    }

}
