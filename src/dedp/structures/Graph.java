package dedp.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.io.GraphFileIO;

public class Graph 
{
	
	public long getVertexCount()
	{
		return this.vertexes.size();
	}
	
	public void SortOutEdgesByWeight()
	{
		for(Vertex v : this.getAllVertexes())
		{
			Collections.sort(v.getOutEdges());
		}
	}
	
	public boolean directedEdgeExists(int from, int to) throws ObjectNotFoundException
	{
		return directedEdgeExists((long)from, (long)to);
	}
	
	public boolean directedEdgeExists(long from, long to) throws ObjectNotFoundException
	{
		boolean connected = true;
		Float weight = getEdgeWeight(from, to);
		if(weight == Float.POSITIVE_INFINITY)
		{
			connected = false;
		}
		return connected;
	}
	
	public float getEdgeWeight(long from, long to) throws ObjectNotFoundException
	{
		float weight = Float.POSITIVE_INFINITY;
		Vertex vFrom = getVertex(from);
		for(Edge e : vFrom.outEdges)
		{
			if(e.getTo().getID() == to /*|| e.getTo().getID() == from*/)
			{
				weight = e.getWeight();
				break;
			}
		}
		return weight;
	}
	
	public long getEdgeCount()
	{
		return this.edges.size();
	}
	
	public Collection<Vertex> getAllVertexes()
	{
		return this.vertexes.values();
	}
	
	public Collection<Edge> getAllEdges()
	{
		return this.edges.values();
	}
	
	public boolean containsEdge(long edgeID)
	{
		return this.edges.containsKey(edgeID);
	}
	
	public boolean containsVertex(long vertexID)
	{
		return this.vertexes.containsKey(vertexID);
	}
	
	
	public Vertex addVertex(long vertexID) throws DuplicateEntryException
	{
		if(vertexes.containsKey(vertexID))
		{
			throw new DuplicateEntryException("Vertex with id " + vertexID + " is already exiting.");
		}
		Vertex vertex = new Vertex();
		vertex.setID(vertexID);
		this.vertexes.put(vertexID, vertex);
		return vertex;
	}
	
	public void removeVertex(long vertexID)
	{
		if(vertexes.containsKey(vertexID))
		{
			Vertex v = this.vertexes.get(vertexID);
			this.vertexes.remove(v);
		}
	}
	
	public Vertex addVertex(Vertex vertex) throws DuplicateEntryException
	{
		if(vertexes.containsKey(vertex.getID()))
		{
			throw new DuplicateEntryException("Vertex with id " + vertex.getID() + " is already exiting.");
		}
		this.vertexes.put(vertex.getID(), vertex);
		return vertex;
	}
	
	public Vertex getVertex(long vertexID) throws ObjectNotFoundException
	{
		return getVertex(vertexID, false);
	}
	
	public Vertex getVertex(long vertexID, boolean addIfNotFound) throws ObjectNotFoundException
	{
		Vertex vertex = null;
		if(vertexes.containsKey(vertexID))
		{
			vertex = this.vertexes.get(vertexID);
		}
		else if(addIfNotFound)
		{
			vertex = new Vertex();
			vertex.setID(vertexID);
			this.vertexes.put(vertexID, vertex);
		}
		else
		{
			throw new ObjectNotFoundException("Vertex with id " + vertexID + " is not found.");
		}
		return vertex;
	}

	public Edge addEdge(long edgeID, long from, long to, float weight, int label, boolean isDirected) throws DuplicateEntryException, ObjectNotFoundException
	{
		return addEdge(edgeID, from, to, weight, label, isDirected, true);
	}
	
	public Edge addEdge(long edgeID, long from, long to, float weight, int label, boolean isDirected, boolean addVertexIfNotFound) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edgeID))
		{
			throw new DuplicateEntryException("Edge with id " + edgeID + " is already exiting.");
		}
		Vertex vFrom = this.getVertex(from, addVertexIfNotFound);
		if(vFrom.fanOut() > 30)
			return null;
		Edge edge = new Edge();
		edge.setID(edgeID);
		Vertex vTo = this.getVertex(to, addVertexIfNotFound);
		edge.setFrom(vFrom);
		edge.setTo(vTo);
		edge.setWeight(weight);
		edge.setLabel(label);
		vFrom.addEdge(edge);
		if(!LabelsIDs.contains(label))
		{
			LabelsIDs.add(label);
			Collections.sort(LabelsIDs);
		}
		if(!isDirected)
		{
			vTo.addEdge(edge);
		}
		this.edges.put(edgeID, edge);
		return edge;
	}
	
	public Edge addEdge(long edgeID, long from, long to, float weight, List<Integer> labels, boolean isDirected, boolean addVertexIfNotFound) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edgeID))
		{
			throw new DuplicateEntryException("Edge with id " + edgeID + " is already exiting.");
		}
		Edge edge = new Edge();
		edge.setID(edgeID);
		Vertex vFrom = this.getVertex(from, addVertexIfNotFound);
		Vertex vTo = this.getVertex(to, addVertexIfNotFound);
		edge.setFrom(vFrom);
		edge.setTo(vTo);
		edge.setWeight(weight);
		//edge.setLabel(label);
		edge.setLabelsSet(labels);
		vFrom.addEdge(edge);
		for(Integer label : labels)
		{
			if(!LabelsIDs.contains(label))
			{
				LabelsIDs.add(label);
				Collections.sort(LabelsIDs);
			}
		}
		if(!isDirected)
		{
			vTo.addEdge(edge);
		}
		this.edges.put(edgeID, edge);
		return edge;
	}
	
	public void removeEdge(Edge e) throws ObjectNotFoundException
	{
		if(!edges.containsKey(e.getID()))
		{
			throw new ObjectNotFoundException("Edge with id " + e.getID() + " is not exiting.");
		}
		e.getFrom().removeEdge(e.getTo());
		this.edges.remove(e.getID());
	}
	
	public Edge addEdge(long edgeID, long from, long to, float weight, String strLabel, boolean isDirected, boolean addVertexIfNotFound) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edgeID))
		{
			throw new DuplicateEntryException("Edge with id " + edgeID + " is already exiting.");
		}
		Edge edge = new Edge();
		edge.setID(edgeID);
		Vertex vFrom = this.getVertex(from, addVertexIfNotFound);
		Vertex vTo = this.getVertex(to, addVertexIfNotFound);
		edge.setFrom(vFrom);
		edge.setTo(vTo);
		edge.setWeight(weight);
		
		Integer labelID;
		if(this.Labels.containsKey(strLabel.intern()))
		{
			labelID = this.Labels.get(strLabel.intern());
		}
		else
		{
			Integer lastLabelID = this.Labels.size();
			this.Labels.put(strLabel.intern(), lastLabelID);
			labelID = lastLabelID;
		}
		edge.setLabel(labelID);
		
		vFrom.addEdge(edge);
		if(!isDirected)
		{
			vTo.addEdge(edge);
		}
		this.edges.put(edgeID, edge);
		return edge;
	}
	
	public Edge addEdge(Edge edge) throws DuplicateEntryException, ObjectNotFoundException
	{
		if(edges.containsKey(edge.getID()))
		{
			throw new DuplicateEntryException("Edge with id " + edge.getID() + " is already exiting.");
		}
		this.edges.put(edge.getID(), edge);
		return edge;
	}
	
	public Graph(int numOfVertexes, int numOfEdges, int numOfLabels, int expectedFanOut)
	{
		vertexes = new HashMap<Long, Vertex>(numOfVertexes);
		edges = new HashMap<Long, Edge>(numOfEdges);
		Labels = new HashMap<String, Integer>(numOfLabels);
		this.expectedFanOut = expectedFanOut;
	}
	
	public int expectedFanOut = -1;
	
	
	public Graph()
	{
		vertexes = new HashMap<Long, Vertex>();
		edges = new HashMap<Long, Edge>();
		Labels = new HashMap<String, Integer>();
	}
	
	public static Graph reverseGraph(Graph forwardGraph) throws DuplicateEntryException, ObjectNotFoundException
	{
		Graph reverse = new Graph();
		for(Vertex v : forwardGraph.getAllVertexes())
		{
			reverse.addVertex(v.getID());
		}
		for(Edge e : forwardGraph.getAllEdges())
		{
			reverse.addEdge(e.getID(), e.getTo().getID(), e.getFrom().getID(), e.getWeight(), e.getLabel(), true, false);
		}
		return reverse;
	}
	
	
	public static Graph copyGraph(Graph graph) throws DuplicateEntryException, ObjectNotFoundException
	{
		Graph copy = new Graph();
		for(Vertex v : graph.getAllVertexes())
		{
			copy.addVertex(v.getID());
		}
		for(Edge e : graph.getAllEdges())
		{
			copy.addEdge(e.getID(), e.getFrom().getID(), e.getTo().getID(), e.getWeight(), e.getLabel(), true, false);
		}
		return copy;
	}
	
	protected Map<Long, Vertex> vertexes;
	public Map<Long, Edge> edges;
	public Map<String, Integer> Labels;
	public ArrayList<Integer> LabelsIDs = new ArrayList<Integer>();
	
	public static void main(String[] args) throws DuplicateEntryException, ObjectNotFoundException 
	{
		// TODO Auto-generated method stub
		Graph g = new Graph();
		g.addVertex(1); g.addVertex(2); g.addVertex(3); g.addVertex(4);
		g.addEdge(1, 1, 2, 5, 1, true);
		g.addEdge(2, 1, 3, 2, 7, true);
		g.addEdge(3, 1, 4, 4, 0, true);
		g.SortOutEdgesByWeight();
		GraphFileIO.printGraph(g);

	}
	
	//for Dynamic EDP
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
		Long[] edgeIDs = new Long[this.edges.keySet().size()];
		this.edges.keySet().toArray(edgeIDs);
		for(int i = 0; i < numOfUpdates; i++)
		{
			//select random edge
			edgeId = edgeIDs[rnd.nextInt(edgeIDs.length)];
			edge = this.edges.get(edgeId);
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
			edge.setWeight(oldWeight * factor);
		}
	}

	public void printStats(){
		System.out.println("Number of vertices is "+vertexes.size());
		System.out.println("Number of edges is "+edges.size());
	}

	

}
