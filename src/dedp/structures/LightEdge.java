package dedp.structures;

public class LightEdge 
{
	public int From;
	public int To;
	public float Weight;
	
	public static LightEdge getDirectedEdgeFromRepresentative(long representative)
	{
		LightEdge lightEdge = new LightEdge();
		lightEdge.From = (int)(representative >> 32);
		lightEdge.To = (int)(representative);
		return lightEdge;
	}
	
	public static long getDirectedEdgeRepresentative(int from, int to)
	{
		long value = 0;
		value = ((long)from << 32);
		value |= to;
		return value;
	}
	
	public static void main(String[] args) throws Exception 
	{
		int from = 2147483647; //2^31 - 1
		int to = 2147483647;
		long representative = LightEdge.getDirectedEdgeRepresentative(from, to);
		LightEdge edge = LightEdge.getDirectedEdgeFromRepresentative(representative);
		System.out.println("Representative: " + representative);
		System.out.println("From: " + edge.From);
		System.out.println("To: " + edge.To);
	}
}
