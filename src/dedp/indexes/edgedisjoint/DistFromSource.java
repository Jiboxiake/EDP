package dedp.indexes.edgedisjoint;

import java.util.List;

import dedp.structures.Edge;
import dedp.structures.Vertex;


public class DistFromSource implements Comparable<DistFromSource>
{
	public int VertexId;
	public int PartitionId;
	public float Distance;
	public boolean goOut = false;
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
	@Override
	public int compareTo(DistFromSource d2) 
	{
		return Float.compare(this.Distance, d2.Distance);
	}
	public static final int NoPartitionExistsId = -1;
}