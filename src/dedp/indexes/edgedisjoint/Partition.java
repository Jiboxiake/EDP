package dedp.indexes.edgedisjoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dedp.algorithms.Dijkstra;
import dedp.common.Constants;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.io.GraphFileIO;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.LightEdge;
import dedp.structures.SPResult;
import dedp.structures.Vertex;

public class Partition 
{
	
	public Partition(int label) throws IOException
	{
		this.Label = label;
		
		File indexFile = new File(getIndexFileName());
		if(!indexFile.exists())
		{
		    indexFile.createNewFile();
		}
		File toBridgeEdgesIndexFile = new File(getToBridgeFileName());
		if(!toBridgeEdgesIndexFile.exists())
		{
			toBridgeEdgesIndexFile.createNewFile();
		}
		File toBridgeEdgesIndexFileBackward = new File(getToBridgeFileName_Backward());
		if(!toBridgeEdgesIndexFileBackward.exists())
		{
			toBridgeEdgesIndexFileBackward.createNewFile();
		}
	}
	
	public void loadSavedIndex() throws IOException, ObjectNotFoundException
	{
		//load the toBridge vertexes
		BufferedReader reader = new BufferedReader(new FileReader(getIndexFileName()));
		String line;
		String[] temp;
		int from, to, timeStamp, pathLength;
		float weight;
		long edgeRepresentative;
		//fill the direct edge weights
		directedEdgeWeights.clear();
		while((line = reader.readLine()) != null)
		{
			temp = line.split(",");
			from = Integer.parseInt(temp[0]);
			to = Integer.parseInt(temp[1]);
			weight = Float.parseFloat(temp[2]);
			timeStamp = Integer.parseInt(temp[3]);
			pathLength = Integer.parseInt(temp[4]);
			edgeRepresentative = LightEdge.getDirectedEdgeRepresentative(from, to);
			//directedEdgeWeights.put(edgeRepresentative, weight);
			directedEdgeWeights.put(edgeRepresentative, new DirectedPathEntry(weight, timeStamp, pathLength));
		}
		reader.close();
		//fill the vertexToBridgeEdges
		int numOfEdges, i;
		ArrayList<PartitionEdge> edges;
		PartitionEdge edge;
		reader = new BufferedReader(new FileReader(getToBridgeFileName()));
		vertexToBridgeEdges.clear();
		while((line = reader.readLine()) != null)
		{
			temp = line.split(",");
			from = Integer.parseInt(temp[0]);
			numOfEdges = Integer.parseInt(temp[1]);
			timeStamp = Integer.parseInt(temp[2]);
			edges = new ArrayList<PartitionEdge>();
			for(i = 0; i < numOfEdges; i++)
			{
				line = reader.readLine();
				temp = line.split(",");
				to = Integer.parseInt(temp[0]);
				weight = Float.parseFloat(temp[1]);
				pathLength = Integer.parseInt(temp[2]);
				edge = new PartitionEdge();
				edge.setFrom(this.getVertex(from));
				edge.setTo(this.getVertex(to));
				edge.setWeight(weight);
				edge.setLabel(this.Label);
				edge.PathLength = pathLength;
				edges.add(edge);
			}
			Collections.sort(edges);
			//vertexToBridgeEdges.put(from, edges);
			vertexToBridgeEdges.put(from, new BridgeEdgesEntry(edges, timeStamp));
			PartitionVertex sourceVertex = this.getVertex(from);
			sourceVertex.numOfBridgeEdgesComputed = edges.size();
			sourceVertex.allBridgeEdgesComputed = true;
		}
		reader.close();
		//fill the vertexToBridgeEdgesBackward
		reader = new BufferedReader(new FileReader(getToBridgeFileName_Backward()));
		vertexToBridgeEdgesBackward.clear();
		while((line = reader.readLine()) != null)
		{
			temp = line.split(",");
			from = Integer.parseInt(temp[0]);
			numOfEdges = Integer.parseInt(temp[1]);
			edges = new ArrayList<PartitionEdge>();
			for(i = 0; i < numOfEdges; i++)
			{
				line = reader.readLine();
				temp = line.split(",");
				to = Integer.parseInt(temp[0]);
				weight = Float.parseFloat(temp[1]);
				edge = new PartitionEdge();
				edge.setFrom(this.getVertex(from));
				edge.setTo(this.getVertex(to));
				edge.setWeight(weight);
				edge.setLabel(this.Label);
				edges.add(edge);
			}
			Collections.sort(edges);
			vertexToBridgeEdgesBackward.put(from, edges);
		}
		reader.close();
	}
	
	public Collection<PartitionVertex> getAllVertexes()
	{
		return this.vertexes.values();
	}
	
	public int vertexCount()
	{
		return this.vertexes.values().size();
	}
	
	public Collection<PartitionEdge> getAllEdges()
	{
		return this.edges.values();
	}
	
	public boolean containsEdge(int edgeID)
	{
		return this.edges.containsKey(edgeID);
	}
	
	public boolean containsVertex(int vertexID)
	{
		return this.vertexes.containsKey(vertexID);
	}
	
	public String getIndexFileName()
	{
		return Constants.ContractedGraphBaseName + this.Label + ".csv";
	}
	
	public String getToBridgeFileName()
	{
		return Constants.ToBridgeEdgesBaseName + this.Label + ".csv";
	}
	
	public String getToBridgeFileName_Backward()
	{
		return Constants.ToBridgeEdgesBaseName + this.Label + "_backward" + ".csv";
	}
	
	public String getToBridgeFileName(int label)
	{
		return Constants.ToBridgeEdgesBaseName + label + ".csv";
	}
	
	public static String getIndexFileName(int label)
	{
		return Constants.ContractedGraphBaseName + label + ".csv";
	}
	
	/*
	public void addIndexEntry(int from, int to, float distance) throws Exception
	{
		//append a line to the end of the file
		Writer output;
		output = new BufferedWriter(new FileWriter(getIndexFileName(), true));
		output.append(from + "," + to + "," + distance + "\n");
		output.close();
	}
	*/
	
	public void addIndexEntry(int from, int to, DirectedPathEntry entry) throws Exception
	{
		//append a line to the end of the file
		Writer output;
		output = new BufferedWriter(new FileWriter(getIndexFileName(), true));
		output.append(from + "," + to + "," + entry.Weight + "," + entry.TimeStamp + "," + entry.PathLength + "\n");
		output.close();
	}
	
	public void addToBridgeIndexEntry(int from, BridgeEdgesEntry entry) throws Exception
	{
		//append a line to the end of the file
		Writer output;
		output = new BufferedWriter(new FileWriter(getToBridgeFileName(), true));
		//write the bridge edges
		output.append(from + "," + entry.BridgeEdges.size() + "," + entry.TimeStamp + "\n"); //format is SourceVertexId,NumberOfToBridgeEdges, timestamp
		for(PartitionEdge edge : entry.BridgeEdges)
		{
			output.append(edge.getTo().getId() + "," + edge.getWeight() + "," + edge.PathLength + "\n"); //format is BridgeVertexId,ShortestDistance
		}
		output.close();
	}
	
	public void addToBridgeIndexEntry_Backward(int from, ArrayList<PartitionEdge> toBridgeEdges) throws Exception
	{
		//append a line to the end of the file
		Writer output;
		output = new BufferedWriter(new FileWriter(getToBridgeFileName_Backward(), true));
		//write the bridge edges
		output.append(from + "," + toBridgeEdges.size() + "\n"); //format is SourceVertexId,NumberOfToBridgeEdges
		for(PartitionEdge edge : toBridgeEdges)
		{
			output.append(edge.getTo().getId() + "," + edge.getWeight() + "\n"); //format is BridgeVertexId,ShortestDistance
		}
		output.close();
	}
	
	public PartitionVertex addVertex(int vertexId) throws DuplicateEntryException
	{
		if(vertexes.containsKey(vertexId))
		{
			throw new DuplicateEntryException("Vertex with id " + vertexId + " is already exiting.");
		}
		PartitionVertex vertex = new PartitionVertex();
		vertex.setId(vertexId);
		this.vertexes.put(vertexId, vertex);
		return vertex;
	}
	
	public void removeVertex(int vertexId)
	{
		if(vertexes.containsKey(vertexId))
		{
			PartitionVertex v = this.vertexes.get(vertexId);
			this.vertexes.remove(v);
		}
	}
	
	public PartitionVertex addVertex(PartitionVertex vertex) throws DuplicateEntryException
	{
		if(vertexes.containsKey(vertex.getId()))
		{
			throw new DuplicateEntryException("Vertex with id " + vertex.getId() + " is already exiting.");
		}
		this.vertexes.put(vertex.getId(), vertex);
		return vertex;
	}
	
	public PartitionVertex getVertex(int vertexId) throws ObjectNotFoundException
	{
		return getVertex(vertexId, false, null, null);
	}
	
	public PartitionVertex getVertex(int vertexId, boolean addIfNotFound, List<Integer> otherLabels, List<Integer> otherLabelsBakcward) throws ObjectNotFoundException
	{
		PartitionVertex vertex = null;
		if(vertexes.containsKey(vertexId))
		{
			vertex = this.vertexes.get(vertexId);
		}
		else if(addIfNotFound)
		{
			vertex = new PartitionVertex();
			vertex.setId(vertexId);
			vertex.setOtherHomes(otherLabels);
			vertex.setOtherHomesBackward(otherLabelsBakcward);
			this.vertexes.put(vertexId, vertex);
		}
		/*
		 //I commented this line as the query processing algorithm calls this function with destination vertexes that may not exist
		else
		{
			throw new ObjectNotFoundException("Vertex with id " + vertexId + " is not found in Partition " + this.Label + ".");
		}
		*/
		return vertex;
	}

	public PartitionEdge addEdge(int edgeID, int from, int to, float weight, int label, boolean isDirected, List<Integer> otherHomes, List<Integer> otherHomesBackward) throws DuplicateEntryException, ObjectNotFoundException
	{
		return addEdge(edgeID, from, to, weight, label, isDirected, true, otherHomes, otherHomesBackward);
	}
	
	public PartitionEdge addEdge(int edgeId, int from, int to, float weight, int label, boolean isDirected, boolean addVertexIfNotFound, List<Integer> otherHomes, List<Integer> otherHomesBackward) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edgeId))
		{
			throw new DuplicateEntryException("Edge with id " + edgeId + " is already exiting.");
		}
		PartitionEdge edge = new PartitionEdge();
		edge.setId(edgeId);
		PartitionVertex vFrom = this.getVertex(from, addVertexIfNotFound, otherHomes, otherHomesBackward);
		PartitionVertex vTo = this.getVertex(to, addVertexIfNotFound, otherHomes, otherHomesBackward);
		edge.setFrom(vFrom);
		edge.setTo(vTo);
		edge.setWeight(weight);
		edge.setLabel(label);
		vFrom.addEdge(edge);
		if(!isDirected)
		{
			vTo.addEdge(edge);
		}
		this.edges.put(edgeId, edge);
		return edge;
	}
	
	public PartitionEdge addEdge(int edgeId, int from, int to, float weight, boolean isDirected, boolean addVertexIfNotFound, List<Integer> otherHomes, List<Integer> otherHomesBackward) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edgeId))
		{
			throw new DuplicateEntryException("Edge with id " + edgeId + " is already exiting.");
		}
		PartitionEdge edge = new PartitionEdge();
		edge.setId(edgeId);
		PartitionVertex vFrom = this.getVertex(from, addVertexIfNotFound, otherHomes, otherHomesBackward);
		PartitionVertex vTo = this.getVertex(to, addVertexIfNotFound, otherHomes, otherHomesBackward);
		edge.setFrom(vFrom);
		edge.setTo(vTo);
		edge.setWeight(weight);
		
		edge.setLabel(this.Label);
		
		vFrom.addEdge(edge);
		if(!isDirected)
		{
			vTo.addEdge(edge);
		}
		this.edges.put(edgeId, edge);
		return edge;
	}
	
	public PartitionEdge addEdge(PartitionEdge edge) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edge.getId()))
		{
			throw new DuplicateEntryException("Edge with id " + edge.getId() + " is already exiting.");
		}
		this.edges.put(edge.getId(), edge);
		return edge;
	}
	
	/*
	public float getEdgeWeight(int from, int to) throws ObjectNotFoundException
	{
		float weight = Float.POSITIVE_INFINITY;
		PartitionVertex vFrom = getVertex(from);
		for(PartitionEdge e : vFrom.outEdges)
		{
			if(e.getTo().getId() == to //|| e.getTo().getId() == from
	)
			{
				weight = e.getWeight();
				break;
			}
		}
		return weight;
	}
	*/
	
	public void print() throws Exception
	{
		//print the vertexes
		System.out.println("Partition labeled : " + this.Label);
		for(PartitionVertex v : this.getAllVertexes())
		{
			System.out.println("ID: " + v.getId() + ", Color: " + v.Label + ", Bridge: " + v.isBridge());
			System.out.println("\tOut edges" );
			for(PartitionEdge e: v.outEdges)
			{
				System.out.println("\t\t --> " + e.getTo().getId() + ", Color: " + e.getLabel() + ", Weight: " + e.getWeight() );
			}
			System.out.println("\tTo bridge edges" );
			for(PartitionEdge e: getToBridgeEdges(v.getId()))
			{
				System.out.println("\t\t --> " + e.getTo().getId() + ", Color: " + e.getLabel() + ", Weight: " + e.getWeight() );
			}
			if(v.isBridge())
			{
				System.out.println("\tOther homes" );
				for(Integer home : v.OtherHomes)
				{
					System.out.println("\t\t --> " + home);
				}
			}
		}
	}
	
	public void printStats()
	{
		//print the vertexes
		System.out.println("Partition labeled : " + this.Label);
		int numOfVertexes = this.getAllVertexes().size();
		int numOfBridgeVertexes = this.bridgeVertexes.size();
		int numOfEdges = this.getAllEdges().size();
		System.out.println("\t Num of vertexes: " + numOfVertexes);
		System.out.println("\t Num of edges: " + numOfEdges);
		System.out.println("\t Num of bridge vertexes: " + numOfBridgeVertexes);
	}
	
	public void updateBridgeVertexes()
	{
		this.bridgeVertexes.clear();
		for(PartitionVertex v : this.getAllVertexes())
		{
			if(v.isBridge())
			{
				this.bridgeVertexes.add(v.getId());
			}
		}
	}
	
	public boolean isBridgeVertex(int vertexId)
	{
		return this.vertexes.get(vertexId).isBridge();
	}
	
	public void updateBridgeVertexesBackward()
	{
		this.bridgeVertexesBackward.clear();
		for(PartitionVertex v : this.getAllVertexes())
		{
			if(v.isBridgeBackward())
			{
				this.bridgeVertexesBackward.add(v.getId());
			}
		}
	}
	
	/*
	public void updateToBridgeEdges() throws Exception
	{
		PartitionVertex from = null;
		PartitionVertex to = null;
		for(Edge e : this.ContractedGraph.getAllEdges())
		{
			to = this.vertexes.get((int)e.getTo().getID());
			if(bridgeVertexes.contains(to.getId()))
			{
				from = this.vertexes.get((int)e.getFrom().getID());
				from.addToBridgeEdge(to, e.getWeight(), e.getLabel());
			}
		}
	}
	*/
	
	public int removeDirectedEdgeWeight(long representative)
	{
		DirectedPathEntry directedPathEntry = directedEdgeWeights.get(representative);
		int length = directedPathEntry.PathLength;
		directedEdgeWeights.remove(representative);
		return length;
	}
	
	public float getEdgeWeight(int from, int to) throws Exception
	{
		long representative = LightEdge.getDirectedEdgeRepresentative(from, to);
		PartitionVertex fromVertex = this.vertexes.get(from);
		//Float weight = directedEdgeWeights.get(representative);
		DirectedPathEntry directedPathEntry = directedEdgeWeights.get(representative);//todo: in DO this is read from distance oracle
		
		//Float weight = directedPathEntry.Weight;
		if(directedPathEntry == null || (directedPathEntry != null && directedPathEntry.TimeStamp < this.ConnectedComponents.getComponentTimeStamp(fromVertex.ComponentId)))
		{
			directedPathEntry = new DirectedPathEntry();
			directedPathEntry.TimeStamp = ConnectedComponents.advanceTimeStamp();
			SPResult directedResult = Dijkstra.shortestDistance(this, from, to);//Construct directly, we want to store a DO entry then
			directedPathEntry.Weight = directedResult.Distance;
			directedPathEntry.PathLength = directedResult.PathLength;
			
			//cache size
			if(Constants.CacheSize != Constants.NoCacheLimit)
			{
				//directedPathEntry.indexEntry = new IndexEntry(this.Label, representative, this.Index.getNextCacheTimeStamp());
				directedPathEntry.indexEntry = new IndexEntry(this.Label, representative);
				this.Index.addIndexEntry(directedPathEntry.indexEntry, directedResult.PathLength);
				if(directedPathsRequested.contains(representative))
				{
					this.Index.recordCacheMiss();
				}
				else
				{
					directedPathsRequested.add(representative);
				}
			}
			
			//directedPathEntry.Weight = Dijkstra.shortestDistance(this, from, to).Distance;
			//save this entry
			//directedEdgeWeights.put(representative, weight);
			directedEdgeWeights.put(representative, directedPathEntry);
			//addIndexEntry(from, to, weight);
			if(Constants.SaveIndexEntriesToDisk)
			{
				addIndexEntry(from, to, directedPathEntry);
			}
		}
		else if(Constants.CacheSize != Constants.NoCacheLimit)
		{
			this.Index.hitIndexEntry(directedPathEntry.indexEntry);
			this.Index.recordCacheHit();
		}
		float weight = directedPathEntry.Weight;
		if (weight == -1)
		{
			weight = Float.POSITIVE_INFINITY;
			//directedPathEntry.Weight = Float.POSITIVE_INFINITY;
		}
		return weight;
		//return directedPathEntry;
	}
	
	public PartitionEdge getToBridgeEdge(int fromVertexId, int edgeOrder) throws Exception
	{
		ArrayList<PartitionEdge> toBridgeEdges = null;
		PartitionEdge partitionEdge = null;
		Float weight = null;
		PartitionVertex sourceVertex = this.getVertex(fromVertexId);
		sourceVertex.lock.lock();
		BridgeEdgesEntry entry = vertexToBridgeEdges.get(fromVertexId);
		if(entry == null || (entry != null && entry.TimeStamp < ConnectedComponents.getComponentTimeStamp(sourceVertex.ComponentId))) //we have to compute it and save it
		{
			//run the thread to start computing the shortcuts
			try
			{
				
				sourceVertex.numOfBridgeEdgesComputed = 0;
				
				//msaber: begin fixing threading issue
				toBridgeEdges = new ArrayList<PartitionEdge>(this.bridgeVertexes.size());
				if(entry == null)
				{
					entry = new BridgeEdgesEntry();
				}
				else
				{
					entry.Thread.NonTerminated = false;
					//entry.Thread.join();
					entry = new BridgeEdgesEntry();
					//vertexToBridgeEdges.remove(entry);
				}
				
				entry.BridgeEdges = toBridgeEdges;
				entry.TimeStamp = ConnectedComponents.advanceTimeStamp();
				vertexToBridgeEdges.put(fromVertexId, entry);//todo: the key will be a morton code for DO
				//while(vertexToBridgeEdges.get(fromVertexId) == null);
				//debugging
				//if(Constants.Debug)
				{
					//if(fromVertexId == 555962 || fromVertexId == 166568 || fromVertexId == 151268 || fromVertexId == 168840 || fromVertexId == 151145)
					{
						//System.out.println("noticee: vertex " + fromVertexId + " is added...");
					}
				}
				//msaber: end fixing threading issue
			}
			finally
			{
				sourceVertex.lock.unlock();
			}
			/*
			toBridgeEdges = new ArrayList<PartitionEdge>(this.bridgeVertexes.size());
			if(entry == null)
			{
				entry = new BridgeEdgesEntry();
			}
			else
			{
				vertexToBridgeEdges.remove(entry);
			}
			entry.BridgeEdges = toBridgeEdges;
			entry.TimeStamp = ConnectedComponents.advanceTimeStamp();
			vertexToBridgeEdges.put(fromVertexId, entry);
			*/
			//run the thread to compute the edges
			BridgeEdgesComputationThread bridgeThread = new BridgeEdgesComputationThread();
			entry.Thread = bridgeThread;
			bridgeThread.fromVertexId = fromVertexId;
			bridgeThread.partition = this;
			bridgeThread.start(); //compute and save to disk (one thread per source vertex);
		}
		else
		{
			toBridgeEdges = entry.BridgeEdges;
			sourceVertex.lock.unlock();
		}
		
		sourceVertex.lock.lock();
		try
		{
			while(edgeOrder >= sourceVertex.numOfBridgeEdgesComputed && !sourceVertex.allBridgeEdgesComputed)
			{
				sourceVertex.bridgeEdgeAdded.await();
			}
		}
		finally
		{
			sourceVertex.lock.unlock();
		}
		if(edgeOrder < sourceVertex.numOfBridgeEdgesComputed)
		{
			partitionEdge = toBridgeEdges.get(edgeOrder);
			entry.NumberOfUsedEdges = Math.max(entry.NumberOfUsedEdges, edgeOrder);
		}
		return partitionEdge;
	}


	public void addToBridgeEdge(int from, int to, DistanceFromSource distFromSource) throws ObjectNotFoundException
	{
		PartitionEdge partitionEdge = new PartitionEdge();
		partitionEdge.setFrom(this.getVertex(from));
		partitionEdge.setTo(this.getVertex(to));
		partitionEdge.setWeight(distFromSource.Distance);
		partitionEdge.PathLength = distFromSource.PathLength;
		while(vertexToBridgeEdges.get(from) == null)
		{
			System.out.println("Vertex with id " + from + " does not exist in partition " + this.Label);
		}
		//vertexToBridgeEdges.get(from).add(partitionEdge);
		vertexToBridgeEdges.get(from).BridgeEdges.add(partitionEdge);
	}

	
	/*
	public void addToBridgeEdge(int from, int to, float weight) throws ObjectNotFoundException
	{
		PartitionEdge partitionEdge = new PartitionEdge();
		partitionEdge.setFrom(this.getVertex(from));
		partitionEdge.setTo(this.getVertex(to));
		partitionEdge.setWeight(weight);
		if(vertexToBridgeEdges.get(from) == null)
		{
			System.out.println("vertex with id " + from + " does not exist in partition " + this.Label);
		}
		//vertexToBridgeEdges.get(from).add(partitionEdge);
		vertexToBridgeEdges.get(from).BridgeEdges.add(partitionEdge);
	}
	*/
	//this is the old blocking version
	
	public ArrayList<PartitionEdge> getToBridgeEdges(int fromVertexId) throws Exception
	{
		//ArrayList<PartitionEdge> toBridgeEdges = vertexToBridgeEdges.get(fromVertexId);
		BridgeEdgesEntry entry = vertexToBridgeEdges.get(fromVertexId);
		PartitionEdge partitionEdge = null;
		Float weight = null;
		PartitionVertex sourceVertex = this.vertexes.get(fromVertexId);
		if(entry == null || (entry != null && entry.TimeStamp < ConnectedComponents.getComponentTimeStamp(sourceVertex.ComponentId))) //we have to compute it and save it
		{
			entry = new BridgeEdgesEntry();
			entry.TimeStamp = ConnectedComponents.advanceTimeStamp();
			entry.BridgeEdges = Dijkstra.shortestDistance(this, fromVertexId, this.bridgeVertexes);
			Collections.sort(entry.BridgeEdges);
			vertexToBridgeEdges.put(fromVertexId, entry);
			//save those computed edges into the "getToBridgeFileName" file
			if(Constants.SaveIndexEntriesToDisk)
			{
				addToBridgeIndexEntry(fromVertexId, entry);
			}
		}
		return entry.BridgeEdges;
	}
	
	public ArrayList<PartitionEdge> getToBridgeEdgesBackward(int fromVertexId) throws Exception
	{
		ArrayList<PartitionEdge> toBridgeEdgesBackward = vertexToBridgeEdgesBackward.get(fromVertexId);
		if(toBridgeEdgesBackward == null) //we have to compute it and save it
		{
			toBridgeEdgesBackward = Dijkstra.shortestDistanceBackward(this, fromVertexId, this.bridgeVertexes);
			Collections.sort(toBridgeEdgesBackward);
			vertexToBridgeEdgesBackward.put(fromVertexId, toBridgeEdgesBackward);
			//save those computed edges into the "getToBridgeFileName" file
			addToBridgeIndexEntry_Backward(fromVertexId, toBridgeEdgesBackward);
		}
		return toBridgeEdgesBackward;
	}
	
	
	public EdgeDisjointIndex Index = null;
	public Graph ContractedGraph = null;
	
	public Collection<Integer> bridgeVertexes = new ArrayList<Integer>();
	public Collection<Integer> bridgeVertexesBackward = new ArrayList<Integer>();
	
	public int Label;
	
	protected Map<Integer, PartitionVertex> vertexes = new HashMap<Integer, PartitionVertex>();
	protected Map<Integer, PartitionEdge> edges = new HashMap<Integer, PartitionEdge>();
	
	
	//protected Map<Long, Float> directedEdgeWeights = new HashMap<Long, Float>();
	protected Map<Long, DirectedPathEntry> directedEdgeWeights = new HashMap<Long, DirectedPathEntry>();
	protected Map<Integer, BridgeEdgesEntry> vertexToBridgeEdges  = new HashMap<Integer, BridgeEdgesEntry>(); //forward
	//protected Map<Integer, ArrayList<PartitionEdge>> vertexToBridgeEdges  = new HashMap<Integer, ArrayList<PartitionEdge>>(); //forward
	protected Map<Integer, ArrayList<PartitionEdge>> vertexToBridgeEdgesBackward  = new HashMap<Integer, ArrayList<PartitionEdge>>(); //backward
	protected Integer[] edgeIDs = null;
	//the following section is specific to the dynamic part
	int totalPathLengthDirected = 0;
	int totalPathLengthBridge = 0;
	
	//the following section is for the cache size
	protected HashSet<Long> directedPathsRequested = new HashSet<Long>();
	
	public void reset()
	{
		this.edges.clear();
		this.vertexes.clear();
		this.directedEdgeWeights.clear();
		this.vertexToBridgeEdges.clear();
		this.vertexToBridgeEdgesBackward.clear();
		this.directedPathsRequested.clear();
		this.edgeIDs = null;
	}
	
	public long getSumPathLengthsDirected()
	{
		long sum = 0;
		for(DirectedPathEntry entry : directedEdgeWeights.values())
		{
			sum += entry.PathLength;
		}
		return sum;
	}
	
	public long getSumPathLengthsBridge()
	{
		long sum = 0;
		for(BridgeEdgesEntry entry : vertexToBridgeEdges.values())
		{
			if(entry.NumberOfUsedEdges >= 0)
			{
				for(int i = 0; i <= entry.NumberOfUsedEdges; i++)
				{
					sum += entry.BridgeEdges.get(i).PathLength;
				}
			}
		}
		return sum;
	}
	
	public PartitionConnectedComponents ConnectedComponents = null;
	
	public int getVertexComponentTimeStamp(PartitionVertex vertex)
	{
		return this.ConnectedComponents.getComponentTimeStamp(vertex.ComponentId);
	}
	
	public void updateEdgeWeight(int edgeId, float newWeight) throws Exception
	{
		PartitionEdge edge = this.edges.get(edgeId);
		edge.setWeight(newWeight);
		int sourceVertexComponentId = edge.getFrom().ComponentId;
		this.ConnectedComponents.advanceComponentTimeStamp(sourceVertexComponentId);
	}
	
	public boolean inTheSameComponent(PartitionVertex v1, PartitionVertex v2)
	{
		return this.ConnectedComponents.inSameComponent(v1, v2);
	}
	
	
	
	
	public static void main(String[] args) throws NumberFormatException, IOException, DuplicateEntryException, ObjectNotFoundException
	{
		String partitionFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\dc_directed\\edge_partition\\graphPartition0";
		Partition p = GraphFileIO.loadPartition(0, partitionFileName, true, true);
		Graph g = GraphFileIO.loadGraph(partitionFileName, true);
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(0);
		int from = 3906, to = 4;
		System.out.println("computing sp...");
		float distance = Dijkstra.shortestDistance(p, from, to).Distance;
		//float distance = Dijkstra.shortestDistance(g, from, to, list).Distance;
		System.out.println("sp distance is " + distance);
	}
	
}
