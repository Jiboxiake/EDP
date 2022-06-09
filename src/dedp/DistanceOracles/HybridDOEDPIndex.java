package dedp.DistanceOracles;

import dedp.algorithms.ConnectedComponentsComputation;
import dedp.common.Constants;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.*;
import dedp.io.GraphFileIO;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.LRUManager;
import dedp.structures.Vertex;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HybridDOEDPIndex {
    public int MaxToExplore = 500;
    public boolean isDirected;



    public static HybridDOEDPIndex buildIndex(Graph graph, List<Integer> excludedPartitions, boolean isDirected) throws Exception
    {
       //HybridDOEDPIndex index = new HybridDOEDPIndex(graph.Labels.size());
        HybridDOEDPIndex index = new HybridDOEDPIndex(graph.LabelsIDs.size(), isDirected);
        index.CacheSize = 0;
        Collection<Edge> edges = graph.getAllEdges();
        Graph contractedGraph = null;
        index.addEdges(edges);
		/*
		for(Edge e : edges)
		{
			index.addEdge(e);
		}
		*/
       /* if(!graph.containsVertex(14218)){
            throw new ObjectNotFoundException("error vertex not found\n");
        }*/
        test(index);
        ConnectedComponentsComputation connectedCompDiscoverer = null;
        int counter=0;
        for(Partition p : index.partitions)
        {
            if(excludedPartitions != null && excludedPartitions.contains(p.Label))
            {
                continue;
            }
            p.updateBridgeVertexes();
            p.sourceGraph = graph;
            //p.updateBridgeVertexesBackward();
            //MSaber: to measure index time only
           // p.loadSavedIndex();
            connectedCompDiscoverer = new ConnectedComponentsComputation();
            connectedCompDiscoverer.buildSCC(p);//construct connected components of this partition
            connectedCompDiscoverer = null;
            //p.vertexes.clear();
            //p.updateToBridgeEdges();
       /*     if(counter==0){
                QuadTree t=index.partitions[0].ConnectedComponents.getConnectedComponent(1).tree;
                HashSet<Integer> testSet = new HashSet<>();
                t.copy(testSet);
                int c = testSet.size();
            }
            counter++;*/
        }
        //long endTime = System.currentTimeMillis();
       // System.out.println("Time for building the index in minutes: " + (double)(endTime - startTime) / (double)60000 + ".");
        index.PlainGraph = graph;
        test(index);
        //index.ReversePlainGraph = Graph.reverseGraph(graph);
        return index;
    }
    public static void test(HybridDOEDPIndex index) throws ObjectNotFoundException {
        for(int i=0; i<index.partitions.length;i++){
            for(Map.Entry<Integer, PartitionEdge>set: index.partitions[i].edges.entrySet()){
                PartitionEdge e = set.getValue();
                if(!index.partitions[i].containsVertex(e.getFrom().getId())||!index.partitions[i].containsVertex(e.getTo().getId())){
                    throw new ObjectNotFoundException("error vertex not found\n");
                }
            }
        }
    }

    public void reset()
    {
        for(Partition p : this.partitions)
        {
            p.reset();
        }
    }

    private HybridDOEDPIndex(int numOfLabels, boolean isDirected) throws IOException
    {
        this.isDirected=false;
        partitions = new Partition[numOfLabels];
        for(int i = 0; i < partitions.length; i++)
        {
            partitions[i] = new Partition(i);
            partitions[i].Index = this;
        }
    }

	/*
	public void reset()
	{

	}
	*/

    public int getNumOfPartitions()
    {
        return this.partitions.length;
    }

    public Partition getPartition(int label)
    {
        return this.partitions[label];
    }

    public Partition[] getAllPartitions()
    {
        return this.partitions;
    }

    public void addEdges(Collection<Edge> edges) throws DuplicateEntryException, ObjectNotFoundException
    {
        if(!isDirected){
            for(Edge e : edges)
            {
                int label = e.getLabel();
                Partition partition = this.partitions[label];
                if(partition.edges.containsKey((int)e.getID()))
                {
                    throw new DuplicateEntryException("Edge with id " + e.getID() + " is already exiting.");
                }
                PartitionEdge edge = new PartitionEdge();
                edge.setId((int)e.getID());
                Vertex eFrom = e.getFrom();
                Vertex eTo = e.getTo();
                List<Integer> outEdgesLabels = new ArrayList<Integer>();
                List<Integer> inEdgesLabels = new ArrayList<Integer>();
                //update the in and out edges of the head of this edge
                for(Edge outE : eFrom.getOutEdges())
                {
                    if(outE.getLabel() != label)
                    {
                        outEdgesLabels.add(outE.getLabel());
                    }
                }
                for(Edge inE : eFrom.getInEdges())
                {
                    if(inE.getLabel() != label)
                    {
                        inEdgesLabels.add(inE.getLabel());
                    }
                }
                //MSaber: to measure index time only
                Collections.sort(outEdgesLabels);
                Collections.sort(inEdgesLabels);
                PartitionVertex vFrom = partition.getVertex((int)eFrom.getID(), true, outEdgesLabels, inEdgesLabels);
                //set coordinates and Morton code
                vFrom.setCoordinates(eFrom.latitude, eFrom.longitude);
                outEdgesLabels.clear();
                inEdgesLabels.clear();
                //update the in and out edges of the tail of this edge
                for(Edge outE : eTo.getOutEdges())
                {
                    if(outE.getLabel() != label)
                    {
                        outEdgesLabels.add(outE.getLabel());
                    }
                }
                for(Edge inE : eTo.getInEdges())
                {
                    if(inE.getLabel() != label)
                    {
                        inEdgesLabels.add(inE.getLabel());
                    }
                }
                //MSaber: to measure index time only
                Collections.sort(outEdgesLabels);
                Collections.sort(inEdgesLabels);
                PartitionVertex vTo = partition.getVertex((int)eTo.getID(), true, outEdgesLabels, inEdgesLabels);
                vTo.setCoordinates(eTo.latitude, eTo.longitude);
                edge.setFrom(vFrom);
                edge.setTo(vTo);
                edge.setWeight(e.getWeight());
                edge.setLabel(label);
                vFrom.addEdge(edge);
                partition.edges.put((int)e.getID(), edge);
                //Global.total_partition_edge++;

                //now we put the reverse edge
                long newID = -1*e.getID();
                if(partition.edges.containsKey((int)newID))
                {
                    throw new DuplicateEntryException("Edge with id " +newID + " is already exiting.");
                }
                edge = new PartitionEdge();
                edge.setId((int)newID);
                eTo = e.getFrom();
                eFrom = e.getTo();
                outEdgesLabels = new ArrayList<Integer>();
                inEdgesLabels = new ArrayList<Integer>();
                //update the in and out edges of the head of this edge
                for(Edge outE : eFrom.getOutEdges())
                {
                    if(outE.getLabel() != label)
                    {
                        outEdgesLabels.add(outE.getLabel());
                    }
                }
                for(Edge inE : eFrom.getInEdges())
                {
                    if(inE.getLabel() != label)
                    {
                        inEdgesLabels.add(inE.getLabel());
                    }
                }
                //MSaber: to measure index time only
                Collections.sort(outEdgesLabels);
                Collections.sort(inEdgesLabels);
                vFrom = partition.getVertex((int)eFrom.getID(), true, outEdgesLabels, inEdgesLabels);
                //set coordinates and Morton code
                outEdgesLabels.clear();
                inEdgesLabels.clear();
                //update the in and out edges of the tail of this edge
                for(Edge outE : eTo.getOutEdges())
                {
                    if(outE.getLabel() != label)
                    {
                        outEdgesLabels.add(outE.getLabel());
                    }
                }
                for(Edge inE : eTo.getInEdges())
                {
                    if(inE.getLabel() != label)
                    {
                        inEdgesLabels.add(inE.getLabel());
                    }
                }
                //MSaber: to measure index time only
                Collections.sort(outEdgesLabels);
                Collections.sort(inEdgesLabels);
                vTo = partition.getVertex((int)eTo.getID(), true, outEdgesLabels, inEdgesLabels);
                edge.setFrom(vFrom);
                edge.setTo(vTo);
                edge.setWeight(e.getWeight());
                edge.setLabel(label);
                vFrom.addEdge(edge);
                partition.edges.put((int)newID, edge);
                //Global.total_partition_edge++;
               /* if(vFrom.getId()==14218 || vTo.getId()==14218){
                    System.out.println(partition.vertexes.containsKey(14218)+" 14218 at partition "+partition.Label);
                }else if(vFrom.getId()==14370 || vTo.getId()==14370){
                    System.out.println(partition.vertexes.containsKey(14370)+" 14370 at partition "+partition.Label);
                }*/
            }
        }else {

            for (Edge e : edges) {
                int label = e.getLabel();
                Partition partition = this.partitions[label];
                if (partition.edges.containsKey((int) e.getID())) {
                    //System.out.println("error");
                    throw new DuplicateEntryException("Edge with id " + e.getID() + " is already exiting.");
                }
                PartitionEdge edge = new PartitionEdge();
                edge.setId((int) e.getID());
                Vertex eFrom = e.getFrom();
                Vertex eTo = e.getTo();
                List<Integer> outEdgesLabels = new ArrayList<Integer>();
                List<Integer> inEdgesLabels = new ArrayList<Integer>();
                //update the in and out edges of the head of this edge
                for (Edge outE : eFrom.getOutEdges()) {
                    if (outE.getLabel() != label) {
                        outEdgesLabels.add(outE.getLabel());
                    }
                }
                for (Edge inE : eFrom.getInEdges()) {
                    if (inE.getLabel() != label) {
                        inEdgesLabels.add(inE.getLabel());
                    }
                }
                //MSaber: to measure index time only
                Collections.sort(outEdgesLabels);
                Collections.sort(inEdgesLabels);
                PartitionVertex vFrom = partition.getVertex((int) eFrom.getID(), true, outEdgesLabels, inEdgesLabels);
                //set coordinates and Morton code
                vFrom.setCoordinates(eFrom.latitude, eFrom.longitude);
                outEdgesLabels.clear();
                inEdgesLabels.clear();
                //update the in and out edges of the tail of this edge
                for (Edge outE : eTo.getOutEdges()) {
                    if (outE.getLabel() != label) {
                        outEdgesLabels.add(outE.getLabel());
                    }
                }
                for (Edge inE : eTo.getInEdges()) {
                    if (inE.getLabel() != label) {
                        inEdgesLabels.add(inE.getLabel());
                    }
                }
                //MSaber: to measure index time only
                Collections.sort(outEdgesLabels);
                Collections.sort(inEdgesLabels);
                PartitionVertex vTo = partition.getVertex((int) eTo.getID(), true, outEdgesLabels, inEdgesLabels);
                vTo.setCoordinates(eTo.latitude, eTo.longitude);
                edge.setFrom(vFrom);
                edge.setTo(vTo);
                edge.setWeight(e.getWeight());
                edge.setLabel(label);
                vFrom.addEdge(edge);
                partition.edges.put((int) e.getID(), edge);
            }
        }

    }


    public PartitionEdge addEdge(Edge e) throws DuplicateEntryException, ObjectNotFoundException
    {
        int label = e.getLabel();
        Partition partition = this.partitions[label];
        if(partition.edges.containsKey(e.getID()))
        {
            throw new DuplicateEntryException("Edge with id " + e.getID() + " is already exiting.");
        }
        PartitionEdge edge = new PartitionEdge();
        edge.setId((int)e.getID());
        Vertex eFrom = e.getFrom();
        Vertex eTo = e.getTo();
        List<Integer> outEdgesLabels = new ArrayList<Integer>();
        List<Integer> inEdgesLabels = new ArrayList<Integer>();
        for(Edge outE : eFrom.getOutEdges())
        {
            if(outE.getLabel() != label)
            {
                outEdgesLabels.add(outE.getLabel());
            }
        }
        for(Edge inE : eFrom.getInEdges())
        {
            if(inE.getLabel() != label)
            {
                inEdgesLabels.add(inE.getLabel());
            }
        }
        Collections.sort(outEdgesLabels);
        Collections.sort(inEdgesLabels);
        PartitionVertex vFrom = partition.getVertex((int)eFrom.getID(), true, outEdgesLabels, inEdgesLabels);
        outEdgesLabels.clear();
        inEdgesLabels.clear();
        for(Edge outE : eTo.getOutEdges())
        {
            if(outE.getLabel() != label)
            {
                outEdgesLabels.add(outE.getLabel());
            }
        }
        for(Edge inE : eTo.getInEdges())
        {
            if(inE.getLabel() != label)
            {
                inEdgesLabels.add(inE.getLabel());
            }
        }
        Collections.sort(outEdgesLabels);
        Collections.sort(inEdgesLabels);
        PartitionVertex vTo = partition.getVertex((int)eTo.getID(), true, outEdgesLabels, inEdgesLabels);
        edge.setFrom(vFrom);
        edge.setTo(vTo);
        edge.setWeight(e.getWeight());
        edge.setLabel(label);
        vFrom.addEdge(edge);
        partition.edges.put((int)e.getID(), edge);
        vFrom.setCoordinates(eFrom.latitude, eFrom.longitude);
        vTo.setCoordinates(eTo.latitude, eTo.longitude);
        return edge;
    }



    //TODO: make this for bridge edges
    public void addIndexEntry(IndexEntry entry, int size)
    {
        if(Constants.CacheSize != Constants.NoCacheLimit)
        {
            if(Constants.CacheSize < (this.CacheSize + size))
            {
                this.freeIndex(size);
            }
            this.CacheSize += size;
            this.hitIndexEntry(entry);
        }
    }

    public void hitIndexEntry(IndexEntry entry)
    {
        if(Constants.CacheSize != Constants.NoCacheLimit)
        {
            this.IndexEntries.remove(entry);
            this.IndexEntries.addLast(entry);
            //this.IndexEntries.remove(entry);
            //this.IndexEntries.addLast(entry);
            //this.IndexEntries.add(entry);
        }
    }

    public void freeIndex(int size)
    {
        if(Constants.CacheSize != Constants.NoCacheLimit)
        {
            int toRemove = size;
            int lastRemovedEntrySize = 0;
            IndexEntry entry = null;
            int maxToRemove = 2;
            int removed = 0;
            while(this.IndexEntries.size() > 0 && toRemove > 0 && removed < maxToRemove)
            {
                entry = this.IndexEntries.removeFirst();
                //entry = this.IndexEntries.poll();
                lastRemovedEntrySize = this.partitions[entry.PartitionId].removeDirectedEdgeWeight(entry.EntryRepresentative);
                toRemove -= lastRemovedEntrySize;
                this.CacheSize -= lastRemovedEntrySize;
                removed++;
            }
        }
    }

    public void recordCacheMiss()
    {
        this.TotalCacheRequests++;
    }

    public void recordCacheHit()
    {
        this.CacheHits++;
        this.TotalCacheRequests++;
    }

    public double getHitRate()
    {
        if(TotalCacheRequests == 0)
            return -1;
        return (double)CacheHits / (double)TotalCacheRequests;
    }

    public long getNextCacheTimeStamp()
    {
        return this.CacheTimeStamp++;
    }


    public Graph PlainGraph = null;
    public Graph ReversePlainGraph = null;

    //public Graph GlobalIndexGraph = null;

    public Partition[] partitions = null;

    //for the cache size...
    public long CacheSize = 0;
    //public LinkedList<IndexEntry> IndexEntries = new LinkedList<IndexEntry>();// LRU are kept at the front
    //public PriorityQueue<IndexEntry> IndexEntries = new PriorityQueue<IndexEntry>();// LRU are thos with the lowest timestamp
    public LRUManager IndexEntries = new LRUManager();
    public long TotalCacheRequests = 0;
    public long CacheHits = 0;
    public long CacheTimeStamp = 0;


    public static void main(String[] args) throws Exception
    {
       
       
       
       
       
       
       
       
       
       
	
	
	
	
	
	
       

    }

    //the following part is for Dynamic EDP

    public void updateEdgeWeights(int numOfUpdates) throws Exception
    {
        Random rnd = new Random(2345678);
        for(int i = 0; i < 2000; i++)
        {
            rnd.nextInt();
        }
        long edgeId;
        boolean isIncrease = false;
        Edge edge = null;
        float factor = 0;
        Long[] edgeIDs = new Long[PlainGraph.edges.keySet().size()];
        PlainGraph.edges.keySet().toArray(edgeIDs);
        Partition partition = null;
        for(int i = 0; i < numOfUpdates; i++)
        {
            long startTime = System.nanoTime();
            //select random edge
            edgeId = edgeIDs[rnd.nextInt(edgeIDs.length)];
            edge = PlainGraph.edges.get(edgeId);
            factor = rnd.nextFloat() + 0.1f;
            isIncrease = false;
            if(rnd.nextFloat() < 0.5)
            {
                isIncrease = true;
            }
            if(isIncrease)
            {
                factor += 1f;
            }
            float oldWeight = edge.getWeight();
            partition = this.partitions[edge.getLabel()];
            edge.setWeight(oldWeight * factor);
            partition.updateEdgeWeight((int)edge.getID(), oldWeight * factor);
            long endTime = System.nanoTime();
            System.out.println("Update," + (endTime - startTime));
        }
    }



	/*
	public void updateEdgeWeights(int numOfUpdates) throws Exception
	{
		//set the edgeIDs list per partition
		for(Partition p : this.partitions)
		{
			p.edgeIDs = new Integer[p.edges.keySet().size()];
			p.edges.keySet().toArray(p.edgeIDs);
		}
		Random rnd = new Random(2345678);
		for(int i = 0; i < 2000; i++)
		{
			rnd.nextInt();
		}
		int partitionId, edgeId;
		boolean isIncrease = false;
		Integer[] edgeIDs = null;
		Partition partition;
		PartitionEdge edge = null;
		float factor = 0;
		for(int i = 0; i < numOfUpdates; i++)
		{
			//select random partition
			partitionId = rnd.nextInt(this.partitions.length);
			partition = this.partitions[partitionId];
			//select random edge
			edgeIDs = partition.edgeIDs;
			edgeId = edgeIDs[rnd.nextInt(edgeIDs.length)];
			edge = partition.edges.get(edgeId);
			factor = rnd.nextFloat() + 0.1f;
			isIncrease = false;
			if(rnd.nextFloat() < 0.5)
			{
				isIncrease = true;
			}
			if(isIncrease)
			{
				factor += 1f;
			}
			partition.updateEdgeWeight(edgeId, edge.getWeight() * factor);
		}
	}
	*/
}
