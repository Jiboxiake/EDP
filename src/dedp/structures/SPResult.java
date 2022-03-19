package dedp.structures;

import dedp.DistanceOracles.BridgeDOThread;
import dedp.DistanceOracles.BridgeEdgeThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SPResult
{
	public float Distance;
	public HashMap<Long, DistanceFromSource> distMap;
	public long NumberOfExploredEdges = 0;
	public long NumberOfExploredNodes = 0;
	public long NumberOfHybridEdgesExplored = 0;
	public long TotalProcessingTime = 0;
	public int PathLength = 0;
	public ArrayList<BridgeEdgeThread>list;
}
