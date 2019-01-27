package dedp.algorithms.hybridtraversal;

import java.util.List;

import dedp.structures.Edge;
import dedp.structures.Vertex;

public class EdgeDisjointQueueEntry implements Comparable<EdgeDisjointQueueEntry>
{
	public int VertexId;
	public int OutEdgeIdToProcess;
	public float Distance;
	public float PotentialDistance;
	public int PartitionId;
	public int PathLength;
	
	public void setPartitionId(Vertex vertex, List<Integer> labelIDs)
	{
		this.PartitionId = NoPartitionExistsId;
		for(Edge e : vertex.outEdges)
		{
			if(labelIDs.contains(e.getLabel()))
			{
				this.PartitionId = e.getLabel();
				break;
			}
		}
		if(this.PartitionId == NoPartitionExistsId)
		{
			//check the in edges
			for(Edge e : vertex.inEdges)
			{
				if(labelIDs.contains(e.getLabel()))
				{
					this.PartitionId = e.getLabel();
					break;
				}
			}
		}
	}
	
	public void setPartitionId_Forward(Vertex vertex, List<Integer> labelIDs)
	{
		this.PartitionId = NoPartitionExistsId;
		for(Edge e : vertex.outEdges)
		{
			if(labelIDs.contains(e.getLabel()))
			{
				this.PartitionId = e.getLabel();
				break;
			}
		}
	}
	
	public void setPartitionId_Backward(Vertex vertex, List<Integer> labelIDs)
	{
		this.PartitionId = NoPartitionExistsId;
		for(Edge e : vertex.inEdges)
		{
			if(labelIDs.contains(e.getLabel()))
			{
				this.PartitionId = e.getLabel();
				break;
			}
		}
	}
	
	
	
	@Override
	public int compareTo(EdgeDisjointQueueEntry d2) 
	{
		//return Float.compare(this.Distance, d2.Distance);
		return Float.compare(this.PotentialDistance, d2.PotentialDistance);
	}
	
	public static final int NoPartitionExistsId = -1;
}
