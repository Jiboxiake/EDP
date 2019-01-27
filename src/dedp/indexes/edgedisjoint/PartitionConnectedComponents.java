package dedp.indexes.edgedisjoint;

import java.util.HashMap;

public class PartitionConnectedComponents 
{
	public PartitionConnectedComponents(int initialNumOfComponents, Partition partition)
	{
		numOfConnectedComponents = initialNumOfComponents;
		capacity = (int)((float)numOfConnectedComponents * (2f - loadFactor));
		componentLastUpdateTimeStamp = new HashMap<Integer, Integer>(capacity, loadFactor);
		for(int i = 0; i < numOfConnectedComponents; i++)
		{
			componentLastUpdateTimeStamp.put(i, 0);
		}
		lastTimeStamp = 0;
		this.partition = partition;
	}
	
	public void addConnectedComponent(int componentId) throws Exception
	{
		if(componentLastUpdateTimeStamp.containsKey(componentId))
		{
			throw new Exception("PartitionConnectedComponents.addConnectedComponent: Component " + componentId + " already exists.");
		}
		this.componentLastUpdateTimeStamp.put(componentId, this.advanceTimeStamp());
	}
	
	public void removeConnectedComponent(int componentId) throws Exception
	{
		if(!componentLastUpdateTimeStamp.containsKey(componentId))
		{
			throw new Exception("PartitionConnectedComponents.removeConnectedComponent: Component " + componentId + " does not exist.");
		}
		this.componentLastUpdateTimeStamp.remove(componentId);
	}
	
	public void advanceComponentTimeStamp(int componentId) throws Exception
	{
		if(!componentLastUpdateTimeStamp.containsKey(componentId))
		{
			throw new Exception("PartitionConnectedComponents.advanceComponentTimeStamp: Component " + componentId + " does not exist.");
		}
		this.componentLastUpdateTimeStamp.put(componentId, this.advanceTimeStamp());
	}
	
	public void mergeConnectedComponents(int component1Id, int component2Id) throws Exception
	{
		if(!componentLastUpdateTimeStamp.containsKey(component2Id) || componentLastUpdateTimeStamp.containsKey(component2Id))
		{
			throw new Exception("PartitionConnectedComponents.mergeConnectedComponents: Component " + component1Id + " or " + component2Id + " does not exist.");
		}
		this.componentLastUpdateTimeStamp.remove(component2Id);
		this.componentLastUpdateTimeStamp.put(component1Id, this.advanceTimeStamp());
		//note: at this point we need to update the connected components vertexes of the partition
	}
	
	public int getLastTimeStamp()
	{
		return lastTimeStamp;
	}
	
	protected int advanceTimeStamp()
	{
		this.lastTimeStamp++;
		return this.lastTimeStamp;
	}
	
	public boolean inSameComponent(PartitionVertex v1, PartitionVertex v2)
	{
		return (v1.ComponentId != -1) && (v1.ComponentId == v2.ComponentId);
	}
	
	public int getComponentTimeStamp(int componentId)
	{
		return componentLastUpdateTimeStamp.get(componentId);
	}
	
	public int getConnectedComponentsCount()
	{
		return this.numOfConnectedComponents;
	}
	 
	//Key = component Id, Value = LastUpdateTimeStamp
	protected Partition partition;
	protected HashMap<Integer, Integer> componentLastUpdateTimeStamp = null;
	protected int lastTimeStamp = 0;
	protected int numOfConnectedComponents = -1;
	protected int capacity = -1;
	protected final float loadFactor = 0.75f;
	
}
