package dedp.algorithms.bidirectional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import dedp.exceptions.ObjectNotFoundException;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;



public class BidirectionalDijkstra 
{
	
	
	public static float getDist(long vId, Map<Long, DistanceFromSource> map)
	{
		Float distance = Float.POSITIVE_INFINITY;
		DistanceFromSource distObj = map.get(vId);
		if(distObj != null)
		{
			distance = distObj.Distance; 
		}
		return distance;
	}
	
	public static SPResult shortestDistance(Graph graph, long source, long destination, List<Integer> labelIDs) throws ObjectNotFoundException
	{
		SPResult result = new SPResult();
		result.Distance = -1;
		result.NumberOfExploredEdges = 0;
		result.NumberOfExploredNodes = 0;
		//intialize single-source
		PriorityQueue<DistanceFromSource> fPQ = new PriorityQueue<DistanceFromSource>();
		PriorityQueue<DistanceFromSource> bPQ = new PriorityQueue<DistanceFromSource>();
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		DistanceFromSource tDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		tDist.VertexID = destination;
		tDist.Distance = 0;
		fPQ.add(uDist);
		bPQ.add(tDist);
		Map<Long, DistanceFromSource> fDistMap = new HashMap<Long, DistanceFromSource>();
		Map<Long, DistanceFromSource> bDistMap = new HashMap<Long, DistanceFromSource>();
		HashSet<Long> fProcessed = new HashSet<Long>();
		HashSet<Long> bProcessed = new HashSet<Long>();
		fDistMap.put(source, uDist);
		bDistMap.put(destination, tDist);
		DistanceFromSource toDist = null;
		float finalDistance = Float.POSITIVE_INFINITY;
		boolean isForwardStep = true;
		long totalStartTime = System.nanoTime();
		while(!fPQ.isEmpty() && !bPQ.isEmpty())
		{
			result.NumberOfExploredNodes++;
			if(isForwardStep/* && !fPQ.isEmpty()*/)
			{
				uDist = fPQ.poll();
				u = graph.getVertex(uDist.VertexID);
				for(Edge e : u.getOutEdges())
				{
					if(labelIDs.contains(e.getLabel()))
					{
						Vertex to = e.getTo();
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = fDistMap.get(to.getID());
						if(toDist == null)
						{
							toDist = new DistanceFromSource();
							toDist.VertexID = to.getID();
							toDist.Distance = Float.POSITIVE_INFINITY;
							fDistMap.put(toDist.VertexID, toDist);
						}
						if(toDist.Distance > uDist.Distance + e.getWeight())
						{
							toDist.Distance = uDist.Distance + e.getWeight();
							fPQ.remove(toDist); //remove if it exists
							fPQ.add(toDist);
						}
						//when scanning an arc (u, w) in the forward search and w is scanned in
						//the reverse search, update µ if df(v) + d(v, w) + dr(w) < µ
						if (uDist.Distance + e.getWeight() +  getDist(to.getID(), bDistMap) < finalDistance)
						{
							finalDistance = uDist.Distance + e.getWeight() +  getDist(to.getID(), bDistMap);
							//System.out.println("Iteration: " + result.NumberOfExploredNodes);
						}
					}
				}
				fProcessed.add(uDist.VertexID);
				if(bProcessed.contains(uDist.VertexID))
				{
					break;
				}
			}
			else// if (!bPQ.isEmpty()) //backward search
			{
				uDist = bPQ.poll();
				u = graph.getVertex(uDist.VertexID);
				for(Edge e : u.getInEdges())
				{
					if(labelIDs.contains(e.getLabel()))
					{
						Vertex to = e.getFrom();
						result.NumberOfExploredEdges++;
						//get the distance of to node
						toDist = bDistMap.get(to.getID());
						if(toDist == null)
						{
							toDist = new DistanceFromSource();
							toDist.VertexID = to.getID();
							toDist.Distance = Float.POSITIVE_INFINITY;
							bDistMap.put(toDist.VertexID, toDist);
						}
						if(toDist.Distance > uDist.Distance + e.getWeight())
						{
							toDist.Distance = uDist.Distance + e.getWeight();
							bPQ.remove(toDist); //remove if it exists
							bPQ.add(toDist);
						}
						//when scanning an arc (u, w) in the forward search and w is scanned in
						//the reverse search, update µ if df(v) + d(v, w) + dr(w) < µ
						if (uDist.Distance + e.getWeight() +  getDist(to.getID(), fDistMap) < finalDistance)
						{
							finalDistance = uDist.Distance + e.getWeight() +  getDist(to.getID(), fDistMap);
						}
					}
				}
				bProcessed.add(uDist.VertexID);
				if(fProcessed.contains(uDist.VertexID))
				{
					break;
				}
			}
			//topf + topr >= µ
			DistanceFromSource fPeek = fPQ.peek();
			DistanceFromSource bPeek = bPQ.peek();
			if(fPeek != null && bPeek != null && fPeek.Distance + bPeek.Distance >= finalDistance)
			{
				break;
			}
			isForwardStep = !isForwardStep;
		}
		result.Distance = finalDistance;
		if(result.Distance == Float.POSITIVE_INFINITY)
		{
			result.Distance = -1;
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
}
