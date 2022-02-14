package dedp.indexes.edgedisjoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dedp.DistanceOracles.BridgeEdgeThread;
import dedp.DistanceOracles.MortonCode;
import dedp.structures.Edge;
import dedp.structures.Vertex;

public class PartitionVertex 
{
	public void setId(int id)
	{
		this.vertexId = id;
	}
	
	public void setOtherHomes(List<Integer> otherHomes)
	{
		this.OtherHomes.clear();
		for(int otherHome : otherHomes)
		{
			this.OtherHomes.add(otherHome);
		}
	}
	
	public void setOtherHomesBackward(List<Integer> otherHomesBackward)
	{
		this.OtherHomes_Backward.clear();
		for(int otherHomeBakcward : otherHomesBackward)
		{
			this.OtherHomes_Backward.add(otherHomeBakcward);
		}
	}
	
	public LinkedList<PartitionEdge> getOutEdges()
	{
		return this.outEdges;
	}
	
	public int getId()
	{
		return this.vertexId;
	}

	//all add edge is adding an out edge.
	public void addEdge(int id, PartitionVertex to, float weight, int label)
	{
		PartitionEdge edge = new PartitionEdge();
		edge.setId(id);
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
	
	public void addEdge(PartitionEdge edge)
	{
		this.outEdges.add(edge);
		if(!edge.getTo().inEdges.contains(edge.getId()))
		{
			edge.getTo().inEdges.add(edge);
			Collections.sort(edge.getTo().inEdges);
		}
		Collections.sort(outEdges);
	}
	
	public void removeEdge(PartitionVertex to)
	{
		PartitionEdge toDelete = null;
		for(PartitionEdge e : outEdges)
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

	public void setCoordinates(int latitude, int longitude){
		this.latitude=latitude;
		this.longitude=longitude;
		this.morton();
	}

	public MortonCode morton(){

		if(mc==null)
		this.mc=new MortonCode(latitude, longitude, 0, true);
		return mc;
	}
	
	public boolean isBridge()
	{
		return (OtherHomes.size() > 0);
	}
	
	public boolean isBridgeBackward()
	{
		return (OtherHomes_Backward.size() > 0);
	}
	
	public boolean allBridgeEdgesComputed = false;
	public int numOfBridgeEdgesComputed = 0;
	public final Lock lock = new ReentrantLock();
	public final Condition bridgeEdgeAdded = lock.newCondition();
	
	public int Label; //the same as label id
	
	public List<Integer> OtherHomes = new ArrayList<Integer>();
	public List<Integer> OtherHomes_Backward = new ArrayList<Integer>();
	
	
	public LinkedList<PartitionEdge> outEdges = new LinkedList<PartitionEdge>();
	public LinkedList<PartitionEdge> inEdges = new LinkedList<PartitionEdge>();
	
	//the coming section is specific to the dynamic part
	public int LocalId = -1;
	public int ComponentId = -1;
	public int longitude;
	public int latitude;
	public MortonCode mc;
	public boolean underBridgeComputation=false;
	public BridgeEdgeThread thread;
	/*
	protected int getToBridgeEdgeId()
	{
		return toBridgeEdges.size() + 1;
	}
	*/
	
	protected int vertexId;
}
