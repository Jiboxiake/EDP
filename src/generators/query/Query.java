package generators.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.ObjectNotFoundException;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.Vertex;

public class Query 
{
	public int Source;
	public int Destination;
	public float Cost;
	public int PathLength;
	public ArrayList<Integer> Labels;
	
	
	public static int shortestDistance(Graph graph, int source, List<Integer> labelIDs, int minHops, int numOfQueries, ArrayList<Query> lstQueries) throws ObjectNotFoundException
	{
		Query query = null;
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>(graph.getAllVertexes().size());
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Integer, DistanceFromSource> distMap = new HashMap<Integer, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		int iterationId = 0;
		int numOfQueriesAdded = 0;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			if(iterationId > minHops)
			{
				query = new Query();
				query.Source = source;
				query.Destination = (int)uDist.VertexID;
				query.Cost = uDist.Distance;
				query.PathLength = iterationId;
				lstQueries.add(query);
				numOfQueriesAdded++;
			}
			for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
			{
				if(labelIDs.contains(e.getLabel()))
				{
					Vertex to = e.getTo();
					//get the distance of to node
					toDist = distMap.get((int)to.getID());
					if(toDist == null)
					{
						toDist = new DistanceFromSource();
						toDist.VertexID = to.getID();
						toDist.Distance = Float.POSITIVE_INFINITY;
						distMap.put((int)toDist.VertexID, toDist);
					}
					if(toDist.Distance > uDist.Distance + e.getWeight())
					{
						toDist.Distance = uDist.Distance + e.getWeight();
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
				}
			}
			iterationId++;
			if(numOfQueriesAdded == numOfQueries)
			{
				break;
			}
		}
		return numOfQueriesAdded;
	}
	
	
	
	public static ArrayList<Query> shortestDistance(Graph graph, int source, List<Integer> labelIDs, int minHops, int numOfQueries) throws ObjectNotFoundException
	{
		ArrayList<Query> lstQueries = new ArrayList<Query>(numOfQueries);
		Query query = null;
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>(graph.getAllVertexes().size());
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Integer, DistanceFromSource> distMap = new HashMap<Integer, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		int iterationId = 0;
		int numOfQueriesAdded = 0;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			if(iterationId > minHops)
			{
				query = new Query();
				query.Source = source;
				query.Destination = (int)uDist.VertexID;
				query.Cost = uDist.Distance;
				query.PathLength = iterationId;
				lstQueries.add(query);
				numOfQueriesAdded++;
			}
			for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
			{
				if(labelIDs.contains(e.getLabel()))
				{
					Vertex to = e.getTo();
					//get the distance of to node
					toDist = distMap.get((int)to.getID());
					if(toDist == null)
					{
						toDist = new DistanceFromSource();
						toDist.VertexID = to.getID();
						toDist.Distance = Float.POSITIVE_INFINITY;
						distMap.put((int)toDist.VertexID, toDist);
					}
					if(toDist.Distance > uDist.Distance + e.getWeight())
					{
						toDist.Distance = uDist.Distance + e.getWeight();
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
				}
			}
			iterationId++;
			if(numOfQueriesAdded == numOfQueries)
			{
				break;
			}
		}
		return lstQueries;
	}
	
	
	public static ArrayList<Query> shortestDistance(Graph graph, int source, int numOfLabels, int minHops, int numOfQueries) throws ObjectNotFoundException
	{
		ArrayList<Query> lstQueries = new ArrayList<Query>();
		Query query = null;
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		Vertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = source;
		uDist.Distance = 0;
		q.add(uDist);
		Map<Integer, DistanceFromSource> distMap = new HashMap<Integer, DistanceFromSource>();
		distMap.put(source, uDist);
		DistanceFromSource toDist = null;
		int iterationId = 0;
		int numOfQueriesAdded = 0;
		while(!q.isEmpty())
		{
			uDist = q.poll();
			u = graph.getVertex(uDist.VertexID);
			if(uDist.Labels.size() == numOfLabels && iterationId > minHops)
			{
				query = new Query();
				query.Source = source;
				query.Destination = (int)uDist.VertexID;
				query.Cost = uDist.Distance;
				query.PathLength = iterationId;
				query.Labels = new ArrayList<Integer>();
				copyElements(uDist.Labels, query.Labels);
				lstQueries.add(query);
				numOfQueriesAdded++;
			}
			for(Edge e : u.getOutEdges()) //here explore only direct monoedges and bridge edges only of the same color
			{
				if( (uDist.Labels.size() < numOfLabels) ||  (uDist.Labels.size() == numOfLabels && uDist.Labels.contains(e.getLabel())))
				{
					Vertex to = e.getTo();
					//get the distance of to node
					toDist = distMap.get((int)to.getID());
					if(toDist == null)
					{
						toDist = new DistanceFromSource();
						toDist.VertexID = to.getID();
						toDist.Distance = Float.POSITIVE_INFINITY;
						toDist.Labels = new ArrayList<Integer>();
						copyElements(uDist.Labels, toDist.Labels);
						distMap.put((int)toDist.VertexID, toDist);
					}
					if(toDist.Distance > uDist.Distance + e.getWeight())
					{
						toDist.Distance = uDist.Distance + e.getWeight();
						q.remove(toDist); //remove if it exists
						q.add(toDist);
					}
					if(!toDist.Labels.contains(e.getLabel()))
					{
						toDist.Labels.add(e.getLabel());
					}
				}
			}
			iterationId++;
			if(numOfQueriesAdded == numOfQueries)
			{
				break;
			}
		}
		return lstQueries;
	}
	
	public static void copyElements(ArrayList<Integer> source, ArrayList<Integer> destination)
	{
		for(int i : source)
		{
			destination.add(i);
		}
	}
	
	
	
	
}
