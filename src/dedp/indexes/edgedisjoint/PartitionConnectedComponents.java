package dedp.indexes.edgedisjoint;

import dedp.DistanceOracles.MortonCode;
//import dedp.DistanceOracles.Node;
import dedp.DistanceOracles.QuadForest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//Now I want to see if we really need a timestamp.
public class PartitionConnectedComponents 
{
/*	//todo: add local varibale distance oracle
	public PartitionConnectedComponents(int initialNumOfComponents, Partition partition, HashMap<Integer, ArrayList<PartitionVertex> >verticesToCC)
	{
		this.verticesToCC= verticesToCC;
		numOfConnectedComponents = initialNumOfComponents;
		capacity = (int)((float)numOfConnectedComponents * (2f - loadFactor));
		componentLastUpdateTimeStamp = new HashMap<Integer, Integer>(capacity, loadFactor);
		for(int i = 0; i < numOfConnectedComponents; i++)
		{
			componentLastUpdateTimeStamp.put(i, 0);
		}
		lastTimeStamp = 0;
		this.partition = partition;
		//TODO: handle merge quadtree nodes and graph vertices
		//Create a list of Morton codes for each CC.
		HashMap<Integer, HashMap<Integer, PartitionVertex>> quadTreeSet= new HashMap<>();
		for(Map.Entry<Integer, ArrayList<PartitionVertex>> cc: verticesToCC.entrySet()){
			if(!quadTreeSet.containsKey(cc.getKey())){
				HashMap<Integer, PartitionVertex> local =new HashMap<MortonCode, PartitionVertex>();
				for(int i=0; i<cc.getValue().size(); i++){
					PartitionVertex v = cc.getValue().get(i);
					local.put(v.morton(), v);
				}
				quadTreeSet.put(cc.getKey(), local);
			}else{
					assert(false);
			}
		}
		//now we create the quadtree for each CC.
		//TODO: check reference between this class and partition
		forest=new QuadForest(quadTreeSet);

	}
	//TODO: handle dynamic graphs
	public void addConnectedComponent(int componentId) throws Exception
	{
		if(componentLastUpdateTimeStamp.containsKey(componentId)||verticesToCC.containsKey(componentId))
		{
			throw new Exception("PartitionConnectedComponents.addConnectedComponent: Component " + componentId + " already exists.");
		}
		this.componentLastUpdateTimeStamp.put(componentId, this.advanceTimeStamp());
		//this.verticesToCC
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
	protected QuadForest forest;
	protected HashMap<Integer, ArrayList<PartitionVertex>>verticesToCC;
	*/

	public PartitionConnectedComponents(int initialNumOfComponents, Partition partition, HashMap<Integer, HashMap<Integer, PartitionVertex> >verticesToCC, HashMap<Integer,HashMap<Integer, PartitionEdge>>edgesToCC, HashMap<Integer, HashMap<Integer, PartitionVertex> >bridgeVerticesToCC){
		this.verticesToCC=verticesToCC;
		this.partition=partition;
		this.bridgeVerticesToCC=bridgeVerticesToCC;
		this.edgesToCC=edgesToCC;
		capacity = (int)((float)numOfConnectedComponents * (2f - loadFactor));
		numOfConnectedComponents=initialNumOfComponents;
		connectedComponents=new HashMap<>();
		for(int i=0; i<verticesToCC.size(); i++){
			assert(verticesToCC.containsKey(i)&&edgesToCC.containsKey(i));
			ConnectedComponent cc = new ConnectedComponent(i, partition, verticesToCC.get(i),edgesToCC.get(i), bridgeVerticesToCC.get(i));
			connectedComponents.put(i, cc);
		}
		this.verticesToCC=null;
		this.bridgeVerticesToCC=null;
		this.edgesToCC=null;
	}

	public boolean inSameComponent(PartitionVertex v1, PartitionVertex v2)
	{
		return (v1.ComponentId != -1) && (v1.ComponentId == v2.ComponentId);
	}

	public int getConnectedComponentsCount()
	{
		return this.numOfConnectedComponents;
	}

	public ConnectedComponent getConnectedComponent(int id){
		return connectedComponents.get(id);
	}

	public void printCC(){
		for(Map.Entry<Integer, ConnectedComponent>e:connectedComponents.entrySet()){
			e.getValue().print();
		}
	}

	public void conditionPrintCC(int num){
		for(Map.Entry<Integer, ConnectedComponent>e:connectedComponents.entrySet()){
			if(e.getValue().vertices.size()>num)
			e.getValue().print();
		}
	}


	protected Partition partition;
	protected int numOfConnectedComponents = -1;
	protected int capacity = -1;
	protected final float loadFactor = 0.75f;
	protected HashMap<Integer, HashMap<Integer, PartitionVertex>>verticesToCC;
	protected HashMap<Integer, HashMap<Integer, PartitionVertex> >bridgeVerticesToCC;
	protected HashMap<Integer,HashMap<Integer, PartitionEdge>>edgesToCC;
	protected HashMap<Integer, ConnectedComponent> connectedComponents;
}
