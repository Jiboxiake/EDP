package dedp.indexes.edgedisjoint;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import dedp.algorithms.ConnectedComponentsComputation;
import dedp.algorithms.FloydWarshal;
import dedp.common.Constants;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.DisjointLabelsIndex;
import dedp.io.GraphFileIO;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.LRUManager;
import dedp.structures.Vertex;


public class EdgeDisjointIndex 
{

	public int MaxToExplore = 5;
	
	public static EdgeDisjointIndex buildIndex(Graph graph, List<Integer> excludedPartitions) throws Exception
	{
		//boolean buildAndSaveIndex = true;
		String indexFolder = Constants.ContractedGraphBaseName.substring(0, Constants.ContractedGraphBaseName.lastIndexOf('/'));
		System.out.println("Deleting contents of " + indexFolder);
		File file = new File(indexFolder);
        File[] files = file.listFiles();
        int numOfFiles = 0;
		if(files!=null) {
			for (File f : files) {
				if (f.isFile() && f.exists()) {
					f.delete();
					numOfFiles++;
				}
			}
		}
		System.out.println("Contents of " + indexFolder + " were deleted (" + numOfFiles + ").");
		long startTime = System.currentTimeMillis();
		EdgeDisjointIndex index = new EdgeDisjointIndex(graph.Labels.size());
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
		ConnectedComponentsComputation connectedCompDiscoverer = null;
		for(Partition p : index.partitions)
		{
			if(excludedPartitions != null && excludedPartitions.contains(p.Label))
			{
				continue;
			}
			p.updateBridgeVertexes();
			//p.updateBridgeVertexesBackward();
			//MSaber: to measure index time only
			//p.loadSavedIndex();
			connectedCompDiscoverer = new ConnectedComponentsComputation();
			connectedCompDiscoverer.buildSCC(p);//construct connected components of this partition
			//p.updateToBridgeEdges();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Time for building the index in minutes: " + (double)(endTime - startTime) / (double)60000 + ".");
		index.PlainGraph = graph;
		//index.ReversePlainGraph = Graph.reverseGraph(graph);
		return index;
	}
	
	public void reset()
	{
		for(Partition p : this.partitions)
		{
			p.reset();
		}
	}
	
	private EdgeDisjointIndex(int numOfLabels) throws IOException
	{
		partitions = new Partition[numOfLabels];
		for(int i = 0; i < partitions.length; i++)
		{
			partitions[i] = new Partition(i);
			/*partitions[i].Index = this;*/
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
		for(Edge e : edges)
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
	
	protected Partition[] partitions = null;
	
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
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		System.out.println("Building hybrid index for " + Constants.GraphFileName + "...");
		EdgeDisjointIndex index = buildIndex(graph, new ArrayList<Integer>());
		System.out.println("Finished building edge disjoint index with " + index.getNumOfPartitions() + " partitions.");
		for(Partition p : index.partitions)
		{
			p.printStats();
			//System.out.println("Saving partition " + p.Label);
			//GraphFileIO.savePartition(p, Constants.ContractedGraphBaseName + "Partition" + p.Label, true);
		}
		/*
		for(Partition p : index.partitions)
		{
			p.printStats();
		}
		*/
		//mark the onBridge edges
		
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
