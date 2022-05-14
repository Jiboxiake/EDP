package dedp.algorithms;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import dedp.common.BytesValue;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.DisjointLabelsIndex;
import dedp.indexes.edgedisjoint.DistFromSource;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.io.GraphFileIO;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;


public class Dijkstra 
{
	public static SPResult shortestDistance(Graph graph, long source, long destination, List<Integer> labelIDs) throws ObjectNotFoundException
	{
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.NumberOfExploredNodes = 0;
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		long totalStartTime = System.nanoTime();
		while(!q.isEmpty())
		{
			result.NumberOfExploredNodes++;
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			if(u.getID() == destination)
			{
				result.Distance = uDist.Distance;
				//System.out.println("Destination was found");
				break;
			}
			for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
			{
				if(labelIDs.contains(e.getLabel()))
				{
					Vertex to = e.getTo();
					result.NumberOfExploredEdges++;
					//get the distance of to node
					toDist = distMap.get(to.getID());
					if(toDist == null)
					{
						toDist = new DistanceFromSource();
						toDist.VertexID = to.getID();
						toDist.Distance = Float.POSITIVE_INFINITY;
						distMap.put(toDist.VertexID, toDist);
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
		long totalEndTime = System.nanoTime();
		/*
		Helper.DebugMsg("Edge disjoint dijkstra: destination checking = " + (destinationChecking));
		Helper.DebugMsg("Edge disjoint dijkstra: isBridge handking logic = " + (bridgeNodeTime));
		Helper.DebugMsg("Edge disjoint dijkstra: exploring bridge edges = " + (exploringExternalEdgesTime));
		*/
		result.TotalProcessingTime = totalEndTime - totalStartTime;
		return result;
	}
	
	
	public static SPResult shortestDistanceRestricted(Graph graph, long source, long destination, List<Integer> R, long lowerThreshold, int maxToRelax, boolean countAddedShortcutsOnly) throws ObjectNotFoundException
	{
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.NumberOfExploredNodes = 0;
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		long totalStartTime = System.nanoTime();
		int count = 0;
		int maxFanExploration = 10;
		int currentFanExploration = 0;
		while(!q.isEmpty())
		{
			result.NumberOfExploredNodes++;
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			if(u.getID() == destination)
			{
				result.Distance = uDist.Distance;
				//System.out.println("Destination was found");
				break;
			}
			currentFanExploration = 0;
			for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
			{
				if(!countAddedShortcutsOnly && e.getTo().ChOrder <= lowerThreshold)
				{
					continue;
				}
				if(Helper.intersection(e.getLabelsSet(), R).size() == 0)
				{
					Vertex to = e.getTo();
					result.NumberOfExploredEdges++;
					//get the distance of to node
					toDist = distMap.get(to.getID());
					if(toDist == null)
					{
						toDist = new DistanceFromSource();
						toDist.VertexID = to.getID();
						toDist.Distance = Float.POSITIVE_INFINITY;
						distMap.put(toDist.VertexID, toDist);
					}
					if(toDist.Distance > uDist.Distance + e.getWeight())
					{
						toDist.Distance = uDist.Distance + e.getWeight();
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
				}
				currentFanExploration++;
				if(currentFanExploration == maxFanExploration)
				{
					break;
				}
			}
			count++;
			if(count == maxToRelax)
			{
				break;
			}
		}
		long totalEndTime = System.nanoTime();
		/*
		Helper.DebugMsg("Edge disjoint dijkstra: destination checking = " + (destinationChecking));
		Helper.DebugMsg("Edge disjoint dijkstra: isBridge handking logic = " + (bridgeNodeTime));
		Helper.DebugMsg("Edge disjoint dijkstra: exploring bridge edges = " + (exploringExternalEdgesTime));
		*/
		result.TotalProcessingTime = totalEndTime - totalStartTime;
		return result;
	}
	
	//seems correct here
	public static SPResult shortestDistance(Partition partition, int source, int destination) throws ObjectNotFoundException
	{
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.PathLength = 0;
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		PartitionVertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		uDist.PathLength = 0;
		q.add(uDist);
		HashMap<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put((long)source, uDist);
		DistanceFromSource toDist = null;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = partition.getVertex((int)uDist.VertexID);
			if(u.getId() == destination)
			{
				result.Distance = uDist.Distance;
				result.PathLength = uDist.PathLength;
				//System.out.println("Destination was found");
				break;
			}
			for(PartitionEdge e : u.outEdges) //here explore only direct monoedges and bridge edges only of the same color
			{
				{
					PartitionVertex to = e.getTo();
					result.NumberOfExploredEdges++;
					//get the distance of to node
					toDist = distMap.get((long)to.getId());
					if(toDist == null)
					{
						toDist = new DistanceFromSource();
						toDist.VertexID = (long)to.getId();
						toDist.Distance = Float.POSITIVE_INFINITY;
						toDist.PathLength = uDist.PathLength + 1;
						distMap.put(toDist.VertexID, toDist);
					}
					if(toDist.Distance > uDist.Distance + e.getWeight())
					{
						toDist.Distance = uDist.Distance + e.getWeight();
						toDist.PathLength = uDist.PathLength + 1;
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
				}
			}
		}
		result.distMap=distMap;
		return result;
	}
	
	
	public static ArrayList<PartitionEdge> shortestDistance(Partition partition, int source, Collection<Integer> destinations) throws ObjectNotFoundException
	{
		ArrayList<PartitionEdge> shortCuts = new ArrayList<PartitionEdge>();
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		PartitionVertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = (long)source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put((long)source, uDist);
		DistanceFromSource toDist = null;
		PartitionEdge partitionEdge;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = partition.getVertex((int)uDist.VertexID);
			for(PartitionEdge e : u.outEdges) //here explore only direct monoedges and bridge edges only of the same color
			{
				PartitionVertex to = e.getTo();
				//get the distance of to node
				toDist = distMap.get((long)to.getId());
				if(toDist == null)
				{
					toDist = new DistanceFromSource();
					toDist.VertexID = (long)to.getId();
					toDist.Distance = uDist.Distance + e.getWeight();
					q.add(toDist);
					distMap.put(toDist.VertexID, toDist);
				}
				else if(toDist.Distance > uDist.Distance + e.getWeight())
				{
					toDist.Distance = uDist.Distance + e.getWeight();
					q.remove(toDist); //remove if it exists
					q.add(toDist);
				}
			}
		}
		PartitionVertex sourceVertex = partition.getVertex(source);
		for(DistanceFromSource dist : distMap.values())
		{
			if(destinations.contains((int)dist.VertexID))
			{
				partitionEdge = new PartitionEdge();
				partitionEdge.setFrom(sourceVertex);
				partitionEdge.setTo(partition.getVertex((int)dist.VertexID));
				partitionEdge.setWeight(dist.Distance);
				shortCuts.add(partitionEdge);
			}
		}
		return shortCuts;
	}
	
	public static ArrayList<PartitionEdge> shortestDistanceBackward(Partition partition, int source, Collection<Integer> destinations) throws ObjectNotFoundException
	{
		ArrayList<PartitionEdge> shortCuts = new ArrayList<PartitionEdge>();
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		PartitionVertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = (long)source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put((long)source, uDist);
		DistanceFromSource toDist = null;
		PartitionEdge partitionEdge;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = partition.getVertex((int)uDist.VertexID);
			for(PartitionEdge e : u.inEdges) //here explore only direct monoedges and bridge edges only of the same color
			{
				PartitionVertex to = e.getFrom(); //we're processing in edges
				//get the distance of to node
				toDist = distMap.get((long)to.getId());
				if(toDist == null)
				{
					toDist = new DistanceFromSource();
					toDist.VertexID = (long)to.getId();
					toDist.Distance = uDist.Distance + e.getWeight();
					q.add(toDist);
					distMap.put(toDist.VertexID, toDist);
				}
				else if(toDist.Distance > uDist.Distance + e.getWeight())
				{
					toDist.Distance = uDist.Distance + e.getWeight();
					q.remove(toDist); //remove if it exists
					q.add(toDist);
				}
			}
		}
		PartitionVertex sourceVertex = partition.getVertex(source);
		for(DistanceFromSource dist : distMap.values())
		{
			if(destinations.contains((int)dist.VertexID))
			{
				partitionEdge = new PartitionEdge();
				partitionEdge.setFrom(sourceVertex);
				partitionEdge.setTo(partition.getVertex((int)dist.VertexID));
				partitionEdge.setWeight(dist.Distance);
				shortCuts.add(partitionEdge);
			}
		}
		return shortCuts;
	}
	
	
	public static SPResult shortestDistanceWithEdgeDisjointIndex(EdgeDisjointIndex index, int source, int destination, List<Integer> labelIDs) throws Exception
	{
		long startTime = System.nanoTime();
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.NumberOfHybridEdgesExplored = 0;
		//
		PriorityQueue<DistFromSource> q = new PriorityQueue<DistFromSource>();
		//maps partition id to a distanceMap
		Map<Integer, Map<Integer, DistFromSource>> partitionToDistMap = new HashMap<Integer, Map<Integer, DistFromSource>>(); 
		for(int label : labelIDs)
		{
			partitionToDistMap.put(label, new HashMap<Integer, DistFromSource>());
		}
		Map<Integer, DistFromSource> distMap = null;
		Map<Integer, DistFromSource> lblDistMap = null;
		DistFromSource lblDist = null;
		PartitionVertex u = null;
		PartitionVertex destVertex = null;
		PartitionVertex toVertex = null;
		DistFromSource uDist = new DistFromSource();
		uDist.VertexId = source;
		uDist.setPartitionId(index.PlainGraph.getVertex(source), labelIDs);
		uDist.Distance = 0;
		uDist.goOut = false;
		if(uDist.PartitionId != DistFromSource.NoPartitionExistsId)
		{
			q.add(uDist);
			partitionToDistMap.get(uDist.PartitionId).put(source, uDist);
		}
		DistFromSource toDist = null;
		float bestDistanceSoFar = Float.POSITIVE_INFINITY;
		Partition currentPartition = null;
		float weight = 0;
		List<Integer> otherHomes = null;
		float lowerBound = Float.POSITIVE_INFINITY; 
		long endTime = System.nanoTime();
		//Helper.DebugMsg("Edge disjoint dijkstra: init time = " + (endTime - startTime));
		long destinationChecking = 0, bridgeNodeTime = 0, exploringExternalEdgesTime = 0, afterDequeueTime = 0; 
		float newDistance = 0;
		boolean furtherExplore = false;
		int iterationId = 0;
		long totalStartTime = System.nanoTime();
		while(!q.isEmpty())
		{
			if(Constants.Debug)
			{
				iterationId++;
				Helper.DebugMsg("Iteration id = " + iterationId);
			}
			startTime = System.nanoTime();
			uDist = q.poll();
			currentPartition = index.getPartition(uDist.PartitionId);
			distMap = partitionToDistMap.get(uDist.PartitionId);
			u = currentPartition.getVertex(uDist.VertexId);
			destVertex = currentPartition.getVertex(destination);
			endTime = System.nanoTime();
			afterDequeueTime += (endTime - startTime);
			if(u.getId() == destination)
			{
				result.Distance = uDist.Distance;
				break;
			}
			if(uDist.Distance >= bestDistanceSoFar)
			//if(uDist.Distance + index.GlobalIndexGraph.getEdgeWeight(u.getId(), destination) > bestDistanceSoFar)
			{
				continue;
				/*
				result.Distance = bestDistanceSoFar;
				Helper.DebugMsg("We will be ending early :)");
				break;
				*/
			}
			//check the destination here
			startTime = System.nanoTime();
			if(destVertex != null)
			{
				try
				{
					//weight = currentPartition.ContractedGraph.getEdgeWeight(uDist.VertexId, destination);
					weight = currentPartition.getEdgeWeight(uDist.VertexId, destination);
					newDistance = uDist.Distance + weight;
				}
				catch (Exception ex)
				{
					System.out.println("Problem in partition " + currentPartition.Label + ", " + uDist.VertexId + " --> " + destination);
					throw ex;
				}
				if(weight != Float.POSITIVE_INFINITY //&& (uDist.Distance + weight < bestDistanceSoFar)
					&& ((newDistance < bestDistanceSoFar) || toDist == null || (toDist != null && (toDist.Distance > newDistance)))	)
				{
					result.NumberOfExploredEdges++;
					//get the distance of to node
					toDist = distMap.get(destination);
					if(toDist == null)
					{
						toDist = new DistFromSource();
						toDist.VertexId = destination;
						toDist.PartitionId = currentPartition.Label;
						//toDist.Distance = Float.POSITIVE_INFINITY;
						toDist.Distance = newDistance;
						q.add(toDist);
						distMap.put(destination, toDist);
					}
					else if (toDist.Distance > newDistance)
					{
						toDist.Distance = newDistance;
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
					if(toDist.Distance < bestDistanceSoFar)
					{
						bestDistanceSoFar = toDist.Distance;
					}
				}
			}
			
			endTime = System.nanoTime();
			destinationChecking += (endTime - startTime);
			
			startTime = System.nanoTime();
			
			if(u.isBridge())
			{
				if(Constants.Debug)
				{
					Helper.DebugMsg("Current node is a bridge with id = " + u.getId());
				}
				otherHomes = Helper.intersection(u.OtherHomes, labelIDs);
				for(int otherHome : otherHomes)
				{
					result.NumberOfHybridEdgesExplored++;
					lblDistMap = partitionToDistMap.get(otherHome);
					lblDist = lblDistMap.get(u.getId());
					if(lblDist == null)
					{
						lblDist = new DistFromSource();
						lblDist.VertexId = u.getId();
						lblDist.PartitionId = otherHome;
						lblDist.Distance = uDist.Distance;
						q.add(lblDist);
						lblDistMap.put(lblDist.VertexId, lblDist);
					}
					else if(lblDist.Distance > uDist.Distance)
					{	
						lblDist.Distance = uDist.Distance;
						q.remove(lblDist); //remove if it exists
						q.add(lblDist);
					}
				}
			}
			
			endTime = System.nanoTime();
			bridgeNodeTime += (endTime - startTime);
			
			startTime = System.nanoTime();
			
			//check the to bridge vertexes
			//for(PartitionEdge e : u.toBridgeEdges) //here explore only edges that lead to the external world
			if(Constants.Debug)
			{
				Helper.DebugMsg("Current node is with id = " + u.getId() + " and we are going to compute the toBridgeEdges");
			}
			int countOfBridgeEdges = 0;
			for(PartitionEdge e : currentPartition.getToBridgeEdges(u.getId())) //here explore only edges that lead to the external world
			{
				countOfBridgeEdges++;
				toVertex = e.getTo();
				toDist = distMap.get(toVertex.getId());
				newDistance = uDist.Distance + e.getWeight();
				if(newDistance >= bestDistanceSoFar || (toDist != null && (toDist.Distance <= newDistance)))
				//if(uDist.Distance + e.getWeight() + index.GlobalIndexGraph.getEdgeWeight(e.getTo().getId(), destination) > bestDistanceSoFar)
				{
					continue;
				}
				result.NumberOfExploredEdges++;
				otherHomes = Helper.intersection(toVertex.OtherHomes, labelIDs);
				furtherExplore = false;
				if(otherHomes.size() > 0)
				{
					//update the distance to the toVertex if we have a shorter way
					if(toDist == null)
					{
						toDist = new DistFromSource();
						toDist.VertexId = toVertex.getId();
						toDist.PartitionId = currentPartition.Label;
						toDist.Distance = newDistance;
						q.add(toDist);
						distMap.put(toDist.VertexId, toDist);
					}
					else if(toDist.Distance > newDistance)
					{	
						toDist.Distance = newDistance;
						q.remove(toDist); //remove if it exists
						q.add(toDist);
						furtherExplore = true;
					}
				}
				if(furtherExplore)
				{
					for(int otherHome : otherHomes)
					{
						result.NumberOfHybridEdgesExplored++;
						lblDistMap = partitionToDistMap.get(otherHome);
						lblDist = lblDistMap.get(toVertex.getId());
						if(lblDist == null)
						{
							lblDist = new DistFromSource();
							lblDist.VertexId = toVertex.getId();
							lblDist.PartitionId = otherHome;
							lblDist.Distance = newDistance;
							q.add(lblDist);
							lblDistMap.put(lblDist.VertexId, lblDist);
						}
						else if(lblDist.Distance > newDistance)
						{	
							lblDist.Distance = newDistance;
							q.remove(lblDist); //remove if it exists
							q.add(lblDist);
						}
					}
				}
			}
			if(Constants.Debug)
			{
				Helper.DebugMsg("Current node is with id = " + u.getId() + " and we just finished computing the toBridgeEdges, thier count is " + countOfBridgeEdges);
			}
			
			endTime = System.nanoTime();
			exploringExternalEdgesTime += (endTime - startTime);
		}
		long totalEndTime = System.nanoTime();
		
		Helper.DebugMsg("Edge disjoint dijkstra: destination checking = " + (destinationChecking));
		Helper.DebugMsg("Edge disjoint dijkstra: isBridge handking logic = " + (bridgeNodeTime));
		Helper.DebugMsg("Edge disjoint dijkstra: exploring bridge edges = " + (exploringExternalEdgesTime));
		Helper.DebugMsg("Edge disjoint dijkstra: first part after dequeue = " + (afterDequeueTime));
		result.TotalProcessingTime = totalEndTime - totalStartTime;
		return result;
	}
	
	
	public static SPResult shortestDistanceWithIndex(DisjointLabelsIndex index, long source, long destination, List<Integer> labelIDs, boolean appromximate) throws ObjectNotFoundException
	{
		Graph graph = index.IndexGraph;
		SPResult result = new SPResult();
		result.Distance = -1;
		
		int hybridLabel = index.HybridLabel;
		int sourceLabel = graph.getVertex(source).Label;
		int destinationLabel = graph.getVertex(destination).Label;
		
		result.NumberOfExploredEdges = 0;
		result.NumberOfHybridEdgesExplored = 0;
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		uDist.goOut = false;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		float bestDistanceSoFar = Float.POSITIVE_INFINITY;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			sourceLabel = graph.getVertex(u.getID()).Label;
			if(u.getID() == destination)
			{
				result.Distance = uDist.Distance;
				//System.out.println("Destination was found");
				break;
			}
			if(uDist.Distance > bestDistanceSoFar)
			{
				continue;
			}
			if(sourceLabel < hybridLabel)
			{
				if(!uDist.goOut)
				{
					if(destinationLabel == sourceLabel)
					{
						BytesValue edgeRepBytes = Helper.getEdgeBytesRepresentative(u.getID(), destination);
						Float weight = index.distanceMap.get(edgeRepBytes);
						
						if(appromximate && weight != null)
						{
							result.Distance = uDist.Distance + weight;
							//System.out.println("Appromximate distance to destination was found");
							return result;
						}
						
						if(weight != null)
						{
							if(uDist.Distance + weight > bestDistanceSoFar)
							{
								continue;
							}
							
							Vertex to = graph.getVertex(destination);
							result.NumberOfExploredEdges++;
							//get the distance of to node
							toDist = distMap.get(to.getID());
							if(toDist == null)
							{
								toDist = new DistanceFromSource();
								toDist.VertexID = to.getID();
								toDist.Distance = Float.POSITIVE_INFINITY;
								toDist.goOut = false;
								distMap.put(toDist.VertexID, toDist);
							}
							if(toDist.Distance > uDist.Distance + weight)
							{
								toDist.Distance = uDist.Distance + weight;
								toDist.goOut = false;
								q.remove(toDist); //remove if it exists
								q.add(toDist);
							}
							
							if(toDist.Distance < bestDistanceSoFar)
							{
								bestDistanceSoFar = toDist.Distance;
							}
						}
					}
					for(Edge e : u.beforeBridgeEdges) //here explore only direct monoedges and bridge edges only of the same color
					{
						if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
						{
							continue;
						}
						//System.out.println("Before Bridges Edges " + u.beforeBridgeEdges.size());
						if(labelIDs.contains(e.getLabel()))
						{
							Vertex to = e.getTo();
							result.NumberOfExploredEdges++;
							//get the distance of to node
							toDist = distMap.get(to.getID());
							if(toDist == null)
							{
								toDist = new DistanceFromSource();
								toDist.VertexID = to.getID();
								toDist.Distance = Float.POSITIVE_INFINITY;
								toDist.goOut = true;
								distMap.put(toDist.VertexID, toDist);
							}
							if(toDist.Distance > uDist.Distance + e.getWeight())
							{
								
								
								toDist.Distance = uDist.Distance + e.getWeight();
								toDist.goOut = true;
								q.remove(toDist); //remove if it exists
								q.add(toDist);
							}
						}
					}
					if(u.onBridge) //we are on bridge, so explore the external world
					{
						for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
						{
							if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
							{
								continue;
							}
							if(labelIDs.contains(e.getLabel()) && e.getTo().Label != sourceLabel)
							{
								Vertex to = e.getTo();
								result.NumberOfExploredEdges++;
								//get the distance of to node
								toDist = distMap.get(to.getID());
								if(toDist == null)
								{
									toDist = new DistanceFromSource();
									toDist.VertexID = to.getID();
									toDist.Distance = Float.POSITIVE_INFINITY;
									toDist.goOut = false;
									distMap.put(toDist.VertexID, toDist);
								}
								if(toDist.Distance > uDist.Distance + e.getWeight())
								{
									toDist.Distance = uDist.Distance + e.getWeight();
									toDist.goOut = false;
									q.remove(toDist); //remove if it exists
									q.add(toDist);
								}
							}
						}
			
					}
				}
				else
				{
					for(Edge e : u.bridgeEdges) //here explore only direct monoedges and bridge edges only of the same color
					{
						if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
						{
							continue;
						}
						if(labelIDs.contains(e.getLabel()))
						{
							Vertex to = e.getTo();
							result.NumberOfExploredEdges++;
							//get the distance of to node
							toDist = distMap.get(to.getID());
							if(toDist == null)
							{
								toDist = new DistanceFromSource();
								toDist.VertexID = to.getID();
								toDist.Distance = Float.POSITIVE_INFINITY;
								toDist.goOut = false;
								distMap.put(toDist.VertexID, toDist);
							}
							if(toDist.Distance > uDist.Distance + e.getWeight())
							{
								toDist.Distance = uDist.Distance + e.getWeight();
								toDist.goOut = false;
								q.remove(toDist); //remove if it exists
								q.add(toDist);
							}
						}
					}
				}
				
			}
			else
			{
				for(Edge e : u.getOutEdges()) //here explore all the edges
				{
					if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
					{
						continue;
					}
					if(labelIDs.contains(e.getLabel()))
					{
						Vertex to = e.getTo();
						result.NumberOfExploredEdges++;
						result.NumberOfHybridEdgesExplored++;
						//get the distance of to node
						toDist = distMap.get(to.getID());
						if(toDist == null)
						{
							toDist = new DistanceFromSource();
							toDist.VertexID = to.getID();
							toDist.Distance = Float.POSITIVE_INFINITY;
							toDist.goOut = false;
							distMap.put(toDist.VertexID, toDist);
						}
						if(toDist.Distance > uDist.Distance + e.getWeight())
						{
							toDist.Distance = uDist.Distance + e.getWeight();
							toDist.goOut = false;
							q.remove(toDist); //remove if it exists
							q.add(toDist);
						}
					}
				}
		
			}
		}
		return result;
	}
	
	
	
	public static SPResult shortestDistanceWithPureIndex(DisjointLabelsIndex index, long source, long destination, List<Integer> labelIDs) throws ObjectNotFoundException
	{
		Graph graph = index.IndexGraph;
		SPResult result = new SPResult();
		result.Distance = -1;
		
		int sourceLabel = graph.getVertex(source).Label;
		int destinationLabel = graph.getVertex(destination).Label;
		
		result.NumberOfExploredEdges = 0;
		//intialize single-source
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		uDist.goOut = false;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		float bestDistanceSoFar = Float.POSITIVE_INFINITY;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			sourceLabel = graph.getVertex(u.getID()).Label;
			if(u.getID() == destination)
			{
				result.Distance = uDist.Distance;
				//System.out.println("Destination was found");
				break;
			}
			/*
			if(uDist.Distance > bestDistanceSoFar)
			{
				continue;
			}
			*/
			//if(!uDist.goOut)
			{
				if(destinationLabel == sourceLabel)
				{
					BytesValue edgeRepBytes = Helper.getEdgeBytesRepresentative(u.getID(), destination);
					Float weight = index.distanceMap.get(edgeRepBytes);
					/*
					if(weight != null)
					{
						result.Distance = uDist.Distance + weight;
						//System.out.println("Appromximate distance to destination was found");
						return result;
					}
					*/
					if(weight != null)
					{
						/*
						if(uDist.Distance + weight > bestDistanceSoFar)
						{
							continue;
						}
						*/
						Vertex to = graph.getVertex(destination);
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = distMap.get(to.getID());
						if(toDist == null)
						{
							toDist = new DistanceFromSource();
							toDist.VertexID = to.getID();
							toDist.Distance = Float.POSITIVE_INFINITY;
							toDist.goOut = false;
							distMap.put(toDist.VertexID, toDist);
						}
						if(toDist.Distance > uDist.Distance + weight)
						{
							toDist.Distance = uDist.Distance + weight;
							toDist.goOut = false;
							q.remove(toDist); //remove if it exists
							q.add(toDist);
						}
						if(toDist.Distance < bestDistanceSoFar)
						{
							bestDistanceSoFar = toDist.Distance;
						}
					}
				}
				for(Edge e : u.beforeBridgeEdges) //here explore only direct monoedges and bridge edges only of the same color
				{
					//System.out.println("Before Bridges Edges " + u.beforeBridgeEdges.size());
					if(labelIDs.contains(e.getLabel()))
					{
						/*
						if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
						{
							continue;
						}
						*/
						Vertex to = e.getTo();
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = distMap.get(to.getID());
						if(toDist == null)
						{
							toDist = new DistanceFromSource();
							toDist.VertexID = to.getID();
							toDist.Distance = Float.POSITIVE_INFINITY;
							toDist.goOut = true;
							distMap.put(toDist.VertexID, toDist);
						}
						if(toDist.Distance > uDist.Distance + e.getWeight())
						{	
							toDist.Distance = uDist.Distance + e.getWeight();
							toDist.goOut = true;
							q.remove(toDist); //remove if it exists
							q.add(toDist);
						}
					}
				}
				if(u.onBridge) //we are on bridge, so explore the external world
				{
					for(Edge e : u.bridgeEdges) //here explore only direct monoedges and bridge edges only of the same color
					{
						if(labelIDs.contains(e.getLabel()) /*&& e.getTo().Label != sourceLabel*/)
						{
							/*
							if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
							{
								continue;
							}
							*/
							Vertex to = e.getTo();
							result.NumberOfExploredEdges++;
							//get the distance of to node
							toDist = distMap.get(to.getID());
							if(toDist == null)
							{
								toDist = new DistanceFromSource();
								toDist.VertexID = to.getID();
								toDist.Distance = Float.POSITIVE_INFINITY;
								toDist.goOut = false;
								distMap.put(toDist.VertexID, toDist);
							}
							if(toDist.Distance > uDist.Distance + e.getWeight())
							{
								toDist.Distance = uDist.Distance + e.getWeight();
								toDist.goOut = false;
								q.remove(toDist); //remove if it exists
								q.add(toDist);
							}
						}
					}
		
				}
			}
			/*
			else
			{
				for(Edge e : u.bridgeEdges)
				{
					if(labelIDs.contains(e.getLabel()))
					{
						if(uDist.Distance + e.getWeight() > bestDistanceSoFar)
						{
							continue;
						}
						Vertex to = e.getTo();
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = distMap.get(to.getID());
						if(toDist == null)
						{
							toDist = new DistanceFromSource();
							toDist.VertexID = to.getID();
							toDist.Distance = Float.POSITIVE_INFINITY;
							toDist.goOut = false;
							distMap.put(toDist.VertexID, toDist);
						}
						if(toDist.Distance > uDist.Distance + e.getWeight())
						{
							toDist.Distance = uDist.Distance + e.getWeight();
							toDist.goOut = false;
							q.remove(toDist); //remove if it exists
							q.add(toDist);
						}
					}
				}
			}
			*/
		}
		return result;
	}
	
	
	public static void main(String[] args) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException 
	{
		/*
		PriorityQueue<Vertex> q = new PriorityQueue<Vertex>();
		Vertex v = new Vertex();
		v.distFromSource = Float.POSITIVE_INFINITY;
		q.add(v);
		Vertex v2 = new Vertex();
		v2.distFromSource = 5;
		q.add(v2);
		Vertex v3 = new Vertex();
		v3.distFromSource = 10;
		q.add(v3);
		
		v2.distFromSource = 20;
		
		while(!q.isEmpty())
		{
			v = q.poll();
			System.out.println(v.distFromSource);
		}
		if(v != null)
			return;
		*/
		/*
		List<Integer> labelIDs = new ArrayList<Integer>();
		labelIDs.add(0);
		labelIDs.add(1);
		labelIDs.add(2);
		String fileName = args[0];
		Graph graph = GraphReader.loadGraph(fileName, true);
		float distance = shortestDistance(graph, 42400013, 403283394, labelIDs).Distance;
		*/
		//float distance = shortestDistance(graph, 42398371, 42398370, labelIDs);
		//System.out.print("Distance is " + distance);
		
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		List<Integer> labelIDs = new ArrayList<Integer>();
		labelIDs.add(0);
		labelIDs.add(1);
		float distance = shortestDistance(graph, 1, 6, labelIDs).Distance;
		System.out.print("Distance is " + distance);
	}
}
	