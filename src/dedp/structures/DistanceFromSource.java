package dedp.structures;

import java.util.ArrayList;

public class DistanceFromSource implements Comparable<DistanceFromSource>
{
	public long VertexID;
	public float Distance;
	public boolean goOut = false;
	public int PathLength;
	public ArrayList<Integer> Labels = new ArrayList<Integer>(); 
	@Override
	public int compareTo(DistanceFromSource d2) 
	{
		return Float.compare(this.Distance, d2.Distance);
	}
}
