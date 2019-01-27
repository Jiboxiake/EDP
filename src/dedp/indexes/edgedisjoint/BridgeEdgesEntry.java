package dedp.indexes.edgedisjoint;

import java.util.ArrayList;

public class BridgeEdgesEntry
{
	public ArrayList<PartitionEdge> BridgeEdges;
	public int NumberOfUsedEdges = -1;
	public int TimeStamp;
	
	public BridgeEdgesComputationThread Thread;
	
	public BridgeEdgesEntry(ArrayList<PartitionEdge> bridgeEdges, int timeStamp)
	{
		this.BridgeEdges = bridgeEdges;
		this.TimeStamp = timeStamp;
	}
	public BridgeEdgesEntry()
	{
		this.BridgeEdges = null;
		this.TimeStamp = -1;
	}
}
