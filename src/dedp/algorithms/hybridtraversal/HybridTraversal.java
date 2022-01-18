package dedp.algorithms.hybridtraversal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.DirectedPathEntry;
import dedp.indexes.edgedisjoint.DistFromSource;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;

public class HybridTraversal 
{
	
	public static SPResult shortestDistance(Graph graph, long source, long destination, List<Integer> labelIDs) throws ObjectNotFoundException
	{
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		//intialize single-source
		PriorityQueue<QueueEntry> readyQ = new PriorityQueue<QueueEntry>();
		Vertex u = null;
		QueueEntry uDist = new QueueEntry();
		uDist.VertexId = (int)source;
		u = graph.getVertex((long)uDist.VertexId);
		uDist.OutEdgeIdToProcess = 0;
		uDist.Distance = 0; //if this is the destination, don't count the outgoing edge as a parameter
		if(u.getOutEdges().size() > 0)
		{
			uDist.PotentialDistance = u.getOutEdges().get(0).getWeight(); //distance should be the cost till the next edge (0 + first edge weight)
		}
		else
		{
			uDist.PotentialDistance = 0;
		}
		readyQ.add(uDist);
		Map<Integer, QueueEntry> distMap = new HashMap<Integer, QueueEntry>();
		distMap.put((int)source, uDist);
		QueueEntry toDist = null;
		LinkedList<Edge> outEdges = null;
		Edge edge = null;
		int i = 0, fanOut = 0;
		int nextEdgeId = 0;
		long totalStartTime = System.nanoTime();
		int iteration = 0;
		Random traversalCoin = new Random(source * destination);
		float traversalGuessThreshold = 0.5f;
		
		while(!readyQ.isEmpty())
		{
			/*
			iteration++;
			Helper.DebugMsg("Iteration: " + iteration);
			*/
			uDist = readyQ.poll();
			u = graph.getVertex((long)uDist.VertexId);
			if(u.getID() == destination)
			{
				result.Distance = uDist.Distance;
				//System.out.println("Destination was found");
				break;
			}
			outEdges = u.getOutEdges();
			fanOut = outEdges.size();
			for(i = uDist.OutEdgeIdToProcess; i < fanOut; i++)
			{
				edge = outEdges.get(i);
				nextEdgeId = i + 1;
				if(labelIDs.contains(edge.getLabel()))
				{
					Vertex to = edge.getTo();
					/*
					if(to.getID() == destination)
					{
						Helper.DebugMsg("Destination was found at iteration " + iteration);
					}
					*/
					result.NumberOfExploredEdges++;
					//get the distance of to node
					toDist = distMap.get((int)to.getID());
					if(toDist == null)
					{
						toDist = new QueueEntry();
						toDist.VertexId = (int)to.getID();
						toDist.OutEdgeIdToProcess = 0;
						toDist.Distance = Float.POSITIVE_INFINITY;
						distMap.put(toDist.VertexId, toDist);
					}
					float toFirstEdge = Float.POSITIVE_INFINITY; 
					LinkedList<Edge> toOutEdges = graph.getVertex((long)toDist.VertexId).getOutEdges();
					if(toDist.Distance > uDist.Distance + edge.getWeight())
					{
						toDist.Distance = uDist.Distance + edge.getWeight();
						if(toOutEdges.size() > 0 && toDist.VertexId != (int)destination)
						{
							toFirstEdge = toOutEdges.get(0).getWeight();
						}
						else
						{
							toFirstEdge = 0;
						}
						toDist.PotentialDistance = toDist.Distance + toFirstEdge;
						readyQ.remove(toDist); //remove if it exists
						readyQ.add(toDist);
					}
					//check if the current node should be put as sleep
					if(nextEdgeId < fanOut && toOutEdges.size() > 0)
					{
						edge = outEdges.get(nextEdgeId);
						//if(uDist.Distance + edge.getWeight() > toDist.PotentialDistance)
						//float rValue = (new Random()).nextFloat();
						//Helper.DebugMsg("Rnd: " + rValue);
						//if(rValue < traversalGuessThreshold)
						{
							//uDist should sleep now and we should break
							uDist.OutEdgeIdToProcess = nextEdgeId;
							uDist.PotentialDistance = uDist.Distance + edge.getWeight();
							readyQ.add(uDist);
							break;
						}
					}
				}
			}
		}
		long totalEndTime = System.nanoTime();
		
		//Helper.DebugMsg("Edge disjoint dijkstra: destination checking = " + (destinationChecking));
		//Helper.DebugMsg("Edge disjoint dijkstra: isBridge handking logic = " + (bridgeNodeTime));
		//Helper.DebugMsg("Edge disjoint dijkstra: exploring bridge edges = " + (exploringExternalEdgesTime));
		
		result.TotalProcessingTime = totalEndTime - totalStartTime;
		return result;
	}

	
	public static SPResult shortestDistanceWithEdgeDisjointIndex(EdgeDisjointIndex index, int source, int destination, List<Integer> labelIDs) throws Exception
	{
		long startTime = System.nanoTime();
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.NumberOfExploredNodes = 0;
		result.NumberOfHybridEdgesExplored = 0;
		//
		PriorityQueue<EdgeDisjointQueueEntry> q = new PriorityQueue<EdgeDisjointQueueEntry>();
		//maps partition id to a distanceMap
		Map<Integer, Map<Integer, EdgeDisjointQueueEntry>> partitionToDistMap = new HashMap<Integer, Map<Integer, EdgeDisjointQueueEntry>>(); 
		for(int label : labelIDs)
		{
			partitionToDistMap.put(label, new HashMap<Integer, EdgeDisjointQueueEntry>());
		}
		Map<Integer, EdgeDisjointQueueEntry> distMap = null;
		Map<Integer, EdgeDisjointQueueEntry> lblDistMap = null;
		ArrayList<PartitionEdge> toBridgeEdges;
		int toBridgeEdgesSize = 0, toBridgeEdgesSizeMinusOne;
		PartitionEdge e;
		int i = 0;
		int countOfBridgeEdges = 0;
		EdgeDisjointQueueEntry lblDist = null;
		Partition currentPartition = null;
		PartitionVertex u = null;
		PartitionVertex destVertex = null;
		PartitionVertex toVertex = null;
		EdgeDisjointQueueEntry uDist = new EdgeDisjointQueueEntry();
		uDist.VertexId = source;
		/*Research: does this choice affects performance?*/
		uDist.setPartitionId(index.PlainGraph.getVertex(source), labelIDs);
		uDist.Distance = 0;
		uDist.PotentialDistance = 0;
		uDist.OutEdgeIdToProcess = 0;
		uDist.PathLength = 0;
		if(uDist.PartitionId != DistFromSource.NoPartitionExistsId)
		{
			q.add(uDist);
			partitionToDistMap.get(uDist.PartitionId).put(source, uDist);
			result.NumberOfExploredNodes++;
		}
		EdgeDisjointQueueEntry toDist = null;
		float bestDistanceSoFar = Float.POSITIVE_INFINITY;
		float weight = 0;
		//DirectedPathEntry directedPathEntry = null;
		List<Integer> otherHomes = null;
		float lowerBound = Float.POSITIVE_INFINITY; 
		long endTime = System.nanoTime();
		//Helper.DebugMsg("Edge disjoint dijkstra: init time = " + (endTime - startTime));
		long destinationChecking = 0, bridgeNodeTime = 0, exploringExternalEdgesTime = 0, afterDequeueTime = 0; 
		float newDistance = 0;
		boolean furtherExplore = false;
		int iterationId = 0;
		Random traversalGuess = new Random(source * destination);
		float traversalGuessThreshold = 0.5f;
		int alreadyExplored = 0;
		int uId, toVertexId;
		long totalStartTime = System.nanoTime();
		while(!q.isEmpty())
		{
			result.NumberOfExploredNodes++;
			uDist = q.poll();
			currentPartition = index.getPartition(uDist.PartitionId);
			distMap = partitionToDistMap.get(uDist.PartitionId);
			u = currentPartition.getVertex(uDist.VertexId);
			uId = uDist.VertexId;
			destVertex = currentPartition.getVertex(destination);
			//check if the current vertex is the destination
			//if it is, we are done and can return this
			if(uId == destination)
			{
				result.Distance = uDist.Distance;
				result.PathLength = uDist.PathLength;
				break;
			}
			//if the destination vertex is in the current partition and in the same CC
			if(destVertex != null && currentPartition.inTheSameComponent(u, destVertex))
			{
				weight = currentPartition.getEdgeWeight(uId, destination);//todo: modify getEdgeWeight using distance oracle
				newDistance = uDist.Distance + weight;
				//directedPathEntry = currentPartition.getEdgeWeight(uId, destination);
				//newDistance = uDist.Distance + directedPathEntry.Weight;
				//if we observe better distance than the best so far
				if(newDistance < bestDistanceSoFar)
				{
					//result.NumberOfExploredEdges++;
					//get the distance of to node
					//check if destination is in the distance map
					toDist = distMap.get(destination);
					if(toDist == null)
					{
						toDist = new EdgeDisjointQueueEntry();
						toDist.VertexId = destination;
						toDist.PartitionId = currentPartition.Label;
						toDist.Distance = newDistance;
						toDist.PotentialDistance = toDist.Distance;
						toDist.OutEdgeIdToProcess = 0;
						q.add(toDist);
						distMap.put(destination, toDist);
					}
					//just like Dij, we find better distance for destination
					else if (toDist.Distance > newDistance)
					{
						toDist.Distance = newDistance;
						toDist.PotentialDistance = newDistance;
						toDist.OutEdgeIdToProcess = 0;
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
					bestDistanceSoFar = newDistance;					
				}
			}
			//if Dij reaches a bridge vertex
			if(u.isBridge())
			{
				otherHomes = Helper.intersection(u.OtherHomes, labelIDs);
				for(int otherHome : otherHomes)
				{
					result.NumberOfHybridEdgesExplored++;
					lblDistMap = partitionToDistMap.get(otherHome);
					lblDist = lblDistMap.get(uId);
					if(lblDist == null)
					{
						lblDist = new EdgeDisjointQueueEntry();
						lblDist.VertexId = uId;
						lblDist.PartitionId = otherHome;
						lblDist.Distance = uDist.Distance;
						lblDist.PotentialDistance = uDist.Distance;
						lblDist.OutEdgeIdToProcess = 0;
						q.add(lblDist);
						lblDistMap.put(lblDist.VertexId, lblDist);
					}
					else if(lblDist.Distance > uDist.Distance)
					{	
						lblDist.Distance = uDist.Distance;
						lblDist.PotentialDistance = uDist.Distance;
						lblDist.OutEdgeIdToProcess = 0;
						lblDist.OutEdgeIdToProcess = 0;
						q.remove(lblDist); //remove if it exists
						q.add(lblDist);
					}
				}
			}
			//toBridgeEdges = currentPartition.getToBridgeEdges(uId);
			//toBridgeEdgesSize = toBridgeEdges.size();
			//toBridgeEdgesSizeMinusOne = toBridgeEdgesSize - 1;
			countOfBridgeEdges = 0;
			for(i = uDist.OutEdgeIdToProcess; ; i++) //here explore only edges that lead to the external world
			{
				//e = toBridgeEdges.get(i);
				e = currentPartition.getToBridgeEdge(uId, i);//todo: check how to use distance oracle for that. A bridge edge maps two sets
				if(e == null)
				{
					break;
				}
				toVertex = e.getTo();//get a specific bridge vertex
				toVertexId = toVertex.getId();
				//reach the max number of edges, in our case don't worry as degree of a vertex will be low.
				if(countOfBridgeEdges == index.MaxToExplore /*&& (i < toBridgeEdgesSizeMinusOne)*/)
				{
					if(toVertexId != destination)
					{
						uDist.OutEdgeIdToProcess = i;
						uDist.PotentialDistance = uDist.Distance + e.getWeight();
						q.add(uDist);
						break;
					}
				}
				
				toDist = distMap.get(toVertexId);
				newDistance = uDist.Distance + e.getWeight();
				//if going to bridge vertex is already more expensive than current explored path to dest, we ignore it
				//or if the explored path to the bridge vertex is less expensive.
				if(newDistance >= bestDistanceSoFar || (toDist != null && (toDist.Distance <= newDistance)))
				{
					continue;
				}//else, if the new distance is better
				countOfBridgeEdges++;
				result.NumberOfExploredEdges++;
				otherHomes = Helper.intersection(toVertex.OtherHomes, labelIDs);
				furtherExplore = false;
				if(otherHomes.size() > 0)
				{
					//update the distance to the toVertex if we have a shorter way
					if(toDist == null)
					{
						toDist = new EdgeDisjointQueueEntry();
						toDist.VertexId = toVertexId;
						toDist.PartitionId = currentPartition.Label;
						toDist.Distance = newDistance;
						toDist.PotentialDistance = newDistance;
						q.add(toDist);
						distMap.put(toDist.VertexId, toDist);
					}
					else if(toDist.Distance > newDistance)
					{	
						toDist.Distance = newDistance;
						toDist.PotentialDistance = newDistance;
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
					furtherExplore = true;
				}
				if(furtherExplore)
				{
					for(int otherHome : otherHomes)
					{
						//get the distance map of the other partition
						lblDistMap = partitionToDistMap.get(otherHome);
						lblDist = lblDistMap.get(toVertexId);
						if(lblDist == null)
						{
							lblDist = new EdgeDisjointQueueEntry();
							lblDist.VertexId = toVertexId;
							lblDist.PartitionId = otherHome;
							lblDist.Distance = newDistance;
							lblDist.PotentialDistance = newDistance;
							q.add(lblDist);
							lblDistMap.put(lblDist.VertexId, lblDist);
						}
						else if(lblDist.Distance > newDistance)
						{	
							lblDist.Distance = newDistance;
							lblDist.PotentialDistance = newDistance;
							q.remove(lblDist); //remove if it exists
							q.add(lblDist);
						}
					}
				}
			}
		}
		long totalEndTime = System.nanoTime();
		
		Helper.DebugMsg("Edge disjoint dijkstra: destination checking = " + (destinationChecking));
		Helper.DebugMsg("Edge disjoint dijkstra: isBridge handking logic = " + (bridgeNodeTime));
		Helper.DebugMsg("Edge disjoint dijkstra: exploring bridge edges = " + (exploringExternalEdgesTime));
		Helper.DebugMsg("Edge disjoint dijkstra: first part after dequeue = " + (afterDequeueTime));
		result.TotalProcessingTime = totalEndTime - totalStartTime;
		return result;
	}

	//TODO: finish this
	public static SPResult shortestDistanceWithEdgeDisjointDistanceOracle(EdgeDisjointIndex index, int source, int destination, List<Integer> labelIDs) throws Exception{
		return null;
	}
	
}
