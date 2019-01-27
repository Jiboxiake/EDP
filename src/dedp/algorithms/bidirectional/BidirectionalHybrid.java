package dedp.algorithms.bidirectional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import dedp.algorithms.hybridtraversal.EdgeDisjointQueueEntry;
import dedp.common.Helper;
import dedp.indexes.edgedisjoint.DistFromSource;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.DistanceFromSource;
import dedp.structures.SPResult;

public class BidirectionalHybrid 
{
	
	public static SPResult shortestDistanceWithEdgeDisjointIndex(EdgeDisjointIndex index, int source, int destination, List<Integer> labelIDs) throws Exception
	{
		long startTime = System.nanoTime();
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.NumberOfExploredNodes = 0;
		result.NumberOfHybridEdgesExplored = 0;
		//
		PriorityQueue<EdgeDisjointQueueEntry> fPQ = new PriorityQueue<EdgeDisjointQueueEntry>();
		PriorityQueue<EdgeDisjointQueueEntry> bPQ = new PriorityQueue<EdgeDisjointQueueEntry>();
		//maps partition id to a distanceMap
		Map<Integer, Map<Integer, EdgeDisjointQueueEntry>> fPartitionToDistMap = new HashMap<Integer, Map<Integer, EdgeDisjointQueueEntry>>();
		Map<Integer, Map<Integer, EdgeDisjointQueueEntry>> bPartitionToDistMap = new HashMap<Integer, Map<Integer, EdgeDisjointQueueEntry>>();
		for(int label : labelIDs)
		{
			fPartitionToDistMap.put(label, new HashMap<Integer, EdgeDisjointQueueEntry>());
			bPartitionToDistMap.put(label, new HashMap<Integer, EdgeDisjointQueueEntry>());
		}
		HashSet<Integer> fProcessed = new HashSet<Integer>();
		HashSet<Integer> bProcessed = new HashSet<Integer>();
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
		//Research: does this choice affects performance?
		EdgeDisjointQueueEntry uDist = new EdgeDisjointQueueEntry();
		uDist.VertexId = source;
		uDist.setPartitionId_Forward(index.PlainGraph.getVertex(source), labelIDs);
		uDist.Distance = 0;
		uDist.PotentialDistance = 0;
		uDist.OutEdgeIdToProcess = 0;
		
		EdgeDisjointQueueEntry tDist = new EdgeDisjointQueueEntry();
		tDist.VertexId = destination;
		tDist.setPartitionId_Backward(index.PlainGraph.getVertex(destination), labelIDs);
		tDist.Distance = 0;
		tDist.PotentialDistance = 0;
		tDist.OutEdgeIdToProcess = 0;
		
		if(uDist.PartitionId != DistFromSource.NoPartitionExistsId && tDist.PartitionId != DistFromSource.NoPartitionExistsId)
		{
			fPQ.add(uDist);
			fPartitionToDistMap.get(uDist.PartitionId).put(source, uDist);
			result.NumberOfExploredNodes++;
			
			bPQ.add(tDist);
			bPartitionToDistMap.get(tDist.PartitionId).put(destination, tDist);
			result.NumberOfExploredNodes++;
		}
		EdgeDisjointQueueEntry toDist = null;
		float bestDistanceSoFar = Float.POSITIVE_INFINITY;
		float weight = 0;
		List<Integer> otherHomes = null;
		List<Integer> otherHomesBackward = null;
		float lowerBound = Float.POSITIVE_INFINITY; 
		long endTime = System.nanoTime();
		//Helper.DebugMsg("Edge disjoint dijkstra: init time = " + (endTime - startTime));
		long destinationChecking = 0, bridgeNodeTime = 0, exploringExternalEdgesTime = 0, afterDequeueTime = 0; 
		float newDistance = 0;
		boolean furtherExplore = false;
		int iterationId = 0;
		Random traversalGuess = new Random(source * destination);
		float traversalGuessThreshold = 0.5f;
		boolean isForwardStep = true;
		int alreadyExplored = 0;
		int uId, toVertexId;
		long totalStartTime = System.nanoTime();
		while(!fPQ.isEmpty() || !bPQ.isEmpty())
		{
			result.NumberOfExploredNodes++;
			if(isForwardStep && !fPQ.isEmpty())
			{
				uDist = fPQ.poll();
				currentPartition = index.getPartition(uDist.PartitionId);
				distMap = fPartitionToDistMap.get(uDist.PartitionId);
				u = currentPartition.getVertex(uDist.VertexId);
				uId = uDist.VertexId;
				destVertex = currentPartition.getVertex(destination);
				//omitted  for the bidirectional search
				/*
				if(uId == destination)
				{
					result.Distance = uDist.Distance;
					break;
				}
				else if(uDist.Distance >= bestDistanceSoFar)
				{
					//continue;
					result.Distance = bestDistanceSoFar;
					break;
				}
				*/
				if(destVertex != null)
				{
					weight = currentPartition.getEdgeWeight(uId, destination);
					newDistance = uDist.Distance + weight;
					if(weight != Float.POSITIVE_INFINITY //&& (uDist.Distance + weight < bestDistanceSoFar)
						&& ((newDistance < bestDistanceSoFar) || toDist == null || (toDist != null && (toDist.Distance > newDistance)))	)
					{
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = distMap.get(destination);
						if(toDist == null)
						{
							toDist = new EdgeDisjointQueueEntry();
							toDist.VertexId = destination;
							toDist.PartitionId = currentPartition.Label;
							toDist.Distance = newDistance;
							toDist.PotentialDistance = toDist.Distance;
							toDist.OutEdgeIdToProcess = 0;
							fPQ.add(toDist);
							distMap.put(destination, toDist);
						}
						else if (toDist.Distance > newDistance)
						{
							toDist.Distance = newDistance;
							toDist.PotentialDistance = newDistance;
							toDist.OutEdgeIdToProcess = 0;
							fPQ.remove(toDist); //remove if it exists
							fPQ.add(toDist);
						}
						if(toDist.Distance < bestDistanceSoFar)
						{
							bestDistanceSoFar = toDist.Distance;
						}
					}
				}
				if(u.isBridge())
				{
					otherHomes = Helper.intersection(u.OtherHomes, labelIDs);
					for(int otherHome : otherHomes)
					{
						result.NumberOfHybridEdgesExplored++;
						lblDistMap = fPartitionToDistMap.get(otherHome);
						lblDist = lblDistMap.get(uId);
						if(lblDist == null)
						{
							lblDist = new EdgeDisjointQueueEntry();
							lblDist.VertexId = uId;
							lblDist.PartitionId = otherHome;
							lblDist.Distance = uDist.Distance;
							lblDist.PotentialDistance = uDist.Distance;
							lblDist.OutEdgeIdToProcess = 0;
							fPQ.add(lblDist);
							lblDistMap.put(lblDist.VertexId, lblDist);
						}
						else if(lblDist.Distance > uDist.Distance)
						{	
							lblDist.Distance = uDist.Distance;
							lblDist.PotentialDistance = uDist.Distance;
							lblDist.OutEdgeIdToProcess = 0;
							lblDist.OutEdgeIdToProcess = 0;
							fPQ.remove(lblDist); //remove if it exists
							fPQ.add(lblDist);
						}
					}
				}
				toBridgeEdges = currentPartition.getToBridgeEdges(uId);
				toBridgeEdgesSize = toBridgeEdges.size();
				toBridgeEdgesSizeMinusOne = toBridgeEdgesSize - 1;
				
				countOfBridgeEdges = 0;
				for(i = uDist.OutEdgeIdToProcess; i < toBridgeEdgesSize; i++) //here explore only edges that lead to the external world
				{
					e = toBridgeEdges.get(i);
					toVertex = e.getTo();
					toVertexId = toVertex.getId();
					if(countOfBridgeEdges == index.MaxToExplore && (i < toBridgeEdgesSizeMinusOne))
					{
						if(toVertexId != destination)
						{
							//uDist.OutEdgeIdToProcess = i+1;
							uDist.OutEdgeIdToProcess = i;
							uDist.PotentialDistance = uDist.Distance + e.getWeight();
							fPQ.add(uDist);
							break;
						}
					}
					countOfBridgeEdges++;
					toDist = distMap.get(toVertexId);
					newDistance = uDist.Distance + e.getWeight();
					if(newDistance >= bestDistanceSoFar || (toDist != null && (toDist.Distance <= newDistance)))
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
							toDist = new EdgeDisjointQueueEntry();
							toDist.VertexId = toVertexId;
							toDist.PartitionId = currentPartition.Label;
							toDist.Distance = newDistance;
							toDist.PotentialDistance = newDistance;
							fPQ.add(toDist);
							distMap.put(toDist.VertexId, toDist);
						}
						else if(toDist.Distance > newDistance)
						{	
							toDist.Distance = newDistance;
							toDist.PotentialDistance = newDistance;
							fPQ.remove(toDist); //remove if it exists
							fPQ.add(toDist);
							furtherExplore = true;
						}
					}
					if(furtherExplore)
					{
						for(int otherHome : otherHomes)
						{
							//result.NumberOfHybridEdgesExplored++;
							lblDistMap = fPartitionToDistMap.get(otherHome);
							lblDist = lblDistMap.get(toVertexId);
							if(lblDist == null)
							{
								lblDist = new EdgeDisjointQueueEntry();
								lblDist.VertexId = toVertexId;
								lblDist.PartitionId = otherHome;
								lblDist.Distance = newDistance;
								lblDist.PotentialDistance = newDistance;
								fPQ.add(lblDist);
								lblDistMap.put(lblDist.VertexId, lblDist);
							}
							else if(lblDist.Distance > newDistance)
							{	
								lblDist.Distance = newDistance;
								lblDist.PotentialDistance = newDistance;
								fPQ.remove(lblDist); //remove if it exists
								fPQ.add(lblDist);
							}
						}
					}
				}
				fProcessed.add(uDist.VertexId);
				if(bProcessed.contains(uDist.VertexId))
				{
					break;
				}
			} else if (!bPQ.isEmpty()) //backward search
			{
				uDist = bPQ.poll();
				currentPartition = index.getPartition(uDist.PartitionId);
				distMap = bPartitionToDistMap.get(uDist.PartitionId);
				u = currentPartition.getVertex(uDist.VertexId);
				uId = uDist.VertexId;
				destVertex = currentPartition.getVertex(source);
				//omitted  for the bidirectional search
				/*
				if(uId == destination)
				{
					result.Distance = uDist.Distance;
					break;
				}
				else if(uDist.Distance >= bestDistanceSoFar)
				{
					//continue;
					result.Distance = bestDistanceSoFar;
					break;
				}
				*/
				if(destVertex != null)
				{
					//weight = currentPartition.getEdgeWeight(uId, destination);
					weight = currentPartition.getEdgeWeight(source, uId);
					newDistance = uDist.Distance + weight;
					if(weight != Float.POSITIVE_INFINITY //&& (uDist.Distance + weight < bestDistanceSoFar)
						&& ((newDistance < bestDistanceSoFar) || toDist == null || (toDist != null && (toDist.Distance > newDistance)))	)
					{
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = distMap.get(source);
						if(toDist == null)
						{
							toDist = new EdgeDisjointQueueEntry();
							toDist.VertexId = source;
							toDist.PartitionId = currentPartition.Label;
							toDist.Distance = newDistance;
							toDist.PotentialDistance = toDist.Distance;
							toDist.OutEdgeIdToProcess = 0;
							bPQ.add(toDist);
							distMap.put(source, toDist);
						}
						else if (toDist.Distance > newDistance)
						{
							toDist.Distance = newDistance;
							toDist.PotentialDistance = newDistance;
							toDist.OutEdgeIdToProcess = 0;
							bPQ.remove(toDist); //remove if it exists
							bPQ.add(toDist);
						}
						if(toDist.Distance < bestDistanceSoFar)
						{
							bestDistanceSoFar = toDist.Distance;
						}
					}
				}
				if(u.isBridgeBackward())
				{
					otherHomes = Helper.intersection(u.OtherHomes_Backward, labelIDs);
					for(int otherHome : otherHomes)
					{
						result.NumberOfHybridEdgesExplored++;
						lblDistMap = bPartitionToDistMap.get(otherHome);
						lblDist = lblDistMap.get(uId);
						if(lblDist == null)
						{
							lblDist = new EdgeDisjointQueueEntry();
							lblDist.VertexId = uId;
							lblDist.PartitionId = otherHome;
							lblDist.Distance = uDist.Distance;
							lblDist.PotentialDistance = uDist.Distance;
							lblDist.OutEdgeIdToProcess = 0;
							bPQ.add(lblDist);
							lblDistMap.put(lblDist.VertexId, lblDist);
						}
						else if(lblDist.Distance > uDist.Distance)
						{	
							lblDist.Distance = uDist.Distance;
							lblDist.PotentialDistance = uDist.Distance;
							lblDist.OutEdgeIdToProcess = 0;
							lblDist.OutEdgeIdToProcess = 0;
							bPQ.remove(lblDist); //remove if it exists
							bPQ.add(lblDist);
						}
					}
				}
				toBridgeEdges = currentPartition.getToBridgeEdgesBackward(uId);
				toBridgeEdgesSize = toBridgeEdges.size();
				toBridgeEdgesSizeMinusOne = toBridgeEdgesSize - 1;
				
				countOfBridgeEdges = 0;
				for(i = uDist.OutEdgeIdToProcess; i < toBridgeEdgesSize; i++) //here explore only edges that lead to the external world
				{
					e = toBridgeEdges.get(i);
					toVertex = e.getTo(); //backward traversal
					toVertexId = toVertex.getId();
					if(countOfBridgeEdges == index.MaxToExplore && (i < toBridgeEdgesSizeMinusOne))
					{
						if(toVertexId != source)
						{
							//uDist.OutEdgeIdToProcess = i+1;
							uDist.OutEdgeIdToProcess = i;
							uDist.PotentialDistance = uDist.Distance + e.getWeight();
							bPQ.add(uDist);
							break;
						}
					}
					countOfBridgeEdges++;
					toDist = distMap.get(toVertexId);
					newDistance = uDist.Distance + e.getWeight();
					if(newDistance >= bestDistanceSoFar || (toDist != null && (toDist.Distance <= newDistance)))
					{
						continue;
					}
					result.NumberOfExploredEdges++;
					otherHomes = Helper.intersection(toVertex.OtherHomes_Backward, labelIDs);
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
							bPQ.add(toDist);
							distMap.put(toDist.VertexId, toDist);
						}
						else if(toDist.Distance > newDistance)
						{	
							toDist.Distance = newDistance;
							toDist.PotentialDistance = newDistance;
							bPQ.remove(toDist); //remove if it exists
							bPQ.add(toDist);
							furtherExplore = true;
						}
					}
					if(furtherExplore)
					{
						for(int otherHome : otherHomes)
						{
							//result.NumberOfHybridEdgesExplored++;
							lblDistMap = bPartitionToDistMap.get(otherHome);
							lblDist = lblDistMap.get(toVertexId);
							if(lblDist == null)
							{
								lblDist = new EdgeDisjointQueueEntry();
								lblDist.VertexId = toVertexId;
								lblDist.PartitionId = otherHome;
								lblDist.Distance = newDistance;
								lblDist.PotentialDistance = newDistance;
								bPQ.add(lblDist);
								lblDistMap.put(lblDist.VertexId, lblDist);
							}
							else if(lblDist.Distance > newDistance)
							{	
								lblDist.Distance = newDistance;
								lblDist.PotentialDistance = newDistance;
								bPQ.remove(lblDist); //remove if it exists
								bPQ.add(lblDist);
							}
						}
					}
				}
				bProcessed.add(uDist.VertexId);
				if(fProcessed.contains(uDist.VertexId))
				{
					break;
				}
			}
			EdgeDisjointQueueEntry fPeek = fPQ.peek();
			EdgeDisjointQueueEntry bPeek = bPQ.peek();
			if(fPeek != null && bPeek != null && fPeek.Distance + bPeek.Distance >= bestDistanceSoFar)
			{
				break;
			}
			isForwardStep = !isForwardStep;
		}
		if(bestDistanceSoFar == Float.POSITIVE_INFINITY)
		{
			bestDistanceSoFar = -1;
		}
		result.Distance = bestDistanceSoFar;
		long totalEndTime = System.nanoTime();
		result.TotalProcessingTime = totalEndTime - totalStartTime;
		return result;
	}
}
