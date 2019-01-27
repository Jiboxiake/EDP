package dedp.indexes.edgedisjoint;

public class DirectedPathEntry
{
	public Float Weight;
	public int TimeStamp;
	public int PathLength;
	public IndexEntry indexEntry;
	
	public DirectedPathEntry(float weight, int timeStamp, int pathLength)
	{
		this.Weight = weight;
		this.TimeStamp = timeStamp;
		this.PathLength = pathLength;
	}
	
	public DirectedPathEntry()
	{
		this.Weight = null;
		this.TimeStamp = -1;
		this.PathLength = -1;
	}

}
