package dedp.indexes;

import java.util.HashMap;
import java.util.Map;

import dedp.common.BytesValue;
import dedp.structures.Graph;

public class DisjointLabelsIndex 
{
	
	public Graph IndexGraph;
	public int HybridLabel;
	public int NumberOfLabels;
	
	public Map<BytesValue, Float> distanceMap = new HashMap<BytesValue, Float>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
