package dedp.structures;

import dedp.DistanceOracles.BridgeDOThread;
import dedp.DistanceOracles.BridgeEdgeThread;

import java.util.ArrayList;

public class SPResult
{
	public float Distance;
	public long NumberOfExploredEdges = 0;
	public long NumberOfExploredNodes = 0;
	public long NumberOfHybridEdgesExplored = 0;
	public long TotalProcessingTime = 0;
	public int PathLength = 0;
	public ArrayList<BridgeEdgeThread>list;
}
