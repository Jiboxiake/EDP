package dedp.structures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Vertex implements Comparable<Vertex>
{
	//public float distFromSource;
	public int Label = -1;
	public boolean onBridge = false;
	
	public void setID(long id)
	{ 
		this.id = id;
	}
	
	public long getID()
	{
		return this.id;
	}
	
	public LinkedList<Edge> getOutEdges()
	{
		return this.outEdges;
	}
	
	public LinkedList<Edge> getInEdges()
	{
		return this.inEdges;
	}
	
	public int fanOut()
	{
		return this.outEdges.size();
	}
	
	public int fanIn()
	{
		return this.inEdges.size();
	}
	
	public List<Vertex> getOutVertexes()
	{
		List<Vertex> vertexIDs = new LinkedList<Vertex>();
		for(Edge e : this.outEdges)
		{
			vertexIDs.add(e.getTo());
		}
		return vertexIDs;
	}
	
	public void addEdge(long id, Vertex to, float weight, int label)
	{
		Edge edge = new Edge();
		edge.setID(id);
		edge.setFrom(this);
		edge.setTo(to);
		edge.setWeight(weight);
		edge.setLabel(label);
		this.outEdges.add(edge);
		if(!to.inEdges.contains(id))
		{
			to.inEdges.add(edge);
			Collections.sort(to.inEdges);
		}
		Collections.sort(outEdges);
	}
	
	public void addEdge(Edge edge)
	{
		this.outEdges.add(edge);
		if(!edge.getTo().inEdges.contains(edge.getID()))
		{
			edge.getTo().inEdges.add(edge);
			Collections.sort(edge.getTo().inEdges);
		}
		Collections.sort(outEdges);
	}
	
	public void removeEdge(Vertex to)
	{
		Edge toDelete = null;
		for(Edge e : outEdges)
		{
			if(e.getTo() == to)
			{
				toDelete = e;
				break;
			}
		}
		this.outEdges.remove(toDelete);
		to.inEdges.remove(toDelete);
		Collections.sort(this.outEdges);
	}
	
	protected long id;
	public long ChWeight = 0;
	public long ChOrder = 0;
	public LinkedList<Edge> outEdges = new LinkedList<Edge>();
	public LinkedList<Edge> inEdges = new LinkedList<Edge>();
	
	public LinkedList<Edge> beforeBridgeEdges = new LinkedList<Edge>();
	
	public LinkedList<Edge> bridgeEdges = new LinkedList<Edge>();
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}
	/*
	@Override
	public int compareTo(Vertex v2) 
	{
		return Float.compare(this.distFromSource, v2.distFromSource);
	}
	*/

	@Override
	public int compareTo(Vertex v) {
		// TODO Auto-generated method stub
		return Long.compare(this.ChWeight, v.ChWeight); //Long.compare(this.id, v.id);
	}
}
