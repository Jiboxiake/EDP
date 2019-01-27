package dedp.algorithms.hybridtraversal;

public class QueueEntry implements Comparable<QueueEntry>
{
	public int VertexId;
	public int OutEdgeIdToProcess;
	public float Distance;
	public float PotentialDistance;
	
	@Override
	public int compareTo(QueueEntry d2) 
	{
		return Float.compare(this.PotentialDistance, d2.PotentialDistance);
	}
}
