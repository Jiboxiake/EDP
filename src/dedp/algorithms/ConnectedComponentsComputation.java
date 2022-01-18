package dedp.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;



import dedp.common.Constants;
import dedp.common.Helper;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionConnectedComponents;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.io.GraphFileIO;
import dedp.structures.Graph;

public class ConnectedComponentsComputation 
{

	
	private boolean[] isVisited;        
    private int[] compIdOfVertex;
    private HashMap<Integer, Integer> globalToLocalId = null; 
    private int currentComponentId;   
    private int numOfComponents;//we get components from 0 to a number-1;
    private Stack<PartitionVertex> stack;
    private ArrayList<PartitionVertex> vertexes;
    private int numOfVertexes;
	HashMap<Integer,HashMap<Integer, PartitionVertex>>verticesToCC;
	HashMap<Integer,HashMap<Integer, PartitionEdge>>edgesToCC;
	HashMap<Integer,HashMap<Integer, PartitionVertex>>bridgeVerticesToCC;
    //TODO: we need to partition the partition vertices into CC's.
    public int buildSCC(Partition partition) 
    {
    	numOfVertexes = partition.vertexCount();
    	vertexes = new ArrayList<PartitionVertex>(partition.getAllVertexes());
        isVisited = new boolean[numOfVertexes];
        compIdOfVertex = new int[numOfVertexes];
        currentComponentId = 0;
        numOfComponents = 0;
        globalToLocalId = new HashMap<Integer, Integer>(numOfVertexes);//map global vertex id to local vertex id
        stack = new Stack<PartitionVertex>();
        
        for(int v = 0; v < numOfVertexes; v++)
        {
        	vertexes.get(v).LocalId = v;
        	vertexes.get(v).ComponentId = -1;
        	globalToLocalId.put(vertexes.get(v).getId(), v);
        	isVisited[v] = false;
        	compIdOfVertex[v] = -1;
        }
		verticesToCC = new HashMap<>();
		edgesToCC = new HashMap<>();
		bridgeVerticesToCC= new HashMap<>();
        for (PartitionVertex vertex : vertexes)
        {
            if (!isVisited[vertex.LocalId])
            {
            	dfs(vertex);
            }
        }
		//also pass the vertex information into the connected components and construct the DO.
		//HashMap<Integer, PartitionVertex>[] verticesToCC = new HashMap[numOfComponents];

		assert(verticesToCC.size()==this.NumOfComponents());
        partition.ConnectedComponents = new PartitionConnectedComponents(this.NumOfComponents(), partition, verticesToCC, edgesToCC, bridgeVerticesToCC);
        return this.NumOfComponents();
    }
    
    private void dfs(PartitionVertex currentVertex) 
    { 
    	stack.clear();
    	stack.push(currentVertex);
    	PartitionVertex reachableVertex = null;
    	while(!stack.isEmpty())
    	{
    		currentVertex = stack.pop();
    		compIdOfVertex[currentVertex.LocalId] = currentComponentId;
    		currentVertex.ComponentId = currentComponentId;
    		isVisited[currentVertex.LocalId] = true;
			//insert the vertex to a hash map containing all vertices of this CC.
			if(!verticesToCC.containsKey(currentVertex.ComponentId)){
				verticesToCC.put(currentVertex.ComponentId, new HashMap<Integer, PartitionVertex>());
			}
			verticesToCC.get(currentVertex.ComponentId).put(currentVertex.getId(), currentVertex);
			if(currentVertex.isBridge()){
				if(!bridgeVerticesToCC.containsKey(currentVertex.ComponentId)){
					bridgeVerticesToCC.put(currentComponentId, new HashMap<Integer, PartitionVertex>());
				}
				bridgeVerticesToCC.get(currentComponentId).put(currentVertex.getId(), currentVertex);
			}
    		//iterate over outedges
    		for(PartitionEdge outEdge : currentVertex.outEdges)
    		{
				if(!edgesToCC.containsKey(currentVertex.ComponentId)){
					edgesToCC.put(currentVertex.ComponentId, new HashMap<Integer, PartitionEdge>());
				}
				edgesToCC.get(currentVertex.ComponentId).put(outEdge.getId(), outEdge);
    			reachableVertex = outEdge.getTo();
    			if(!isVisited[reachableVertex.LocalId])
    			{
    				stack.push(reachableVertex);
    			}
    		}
    		//iterate over inedges
    		for(PartitionEdge inEdge : currentVertex.inEdges)
    		{
				if(!edgesToCC.containsKey(currentVertex.ComponentId)){
					edgesToCC.put(currentVertex.ComponentId, new HashMap<Integer, PartitionEdge>());
				}
				edgesToCC.get(currentVertex.ComponentId).put(inEdge.getId(), inEdge);
    			reachableVertex = inEdge.getFrom();
    			if(!isVisited[reachableVertex.LocalId])
    			{
    				stack.push(reachableVertex);
    			}
    		}
    	}
    	currentComponentId++;
    	numOfComponents = currentComponentId;
    }

	private void groupVertices(HashMap<Integer, ArrayList<PartitionVertex>>verticesToCC){
		for(int i=0; i<numOfVertexes; i++){

			//add vertices to the corresponding connected component
			verticesToCC.get(compIdOfVertex[vertexes.get(i).LocalId]).add(vertexes.get(i));
		}
	}
    
    /**
     * Returns the number of strong components.
     * @return the number of strong components
     */
    public int NumOfComponents() 
    {
        return this.numOfComponents;
    }
    
    /**
     * Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
     * @param v one vertex
     * @param w the other vertex
     * @return <tt>true</tt> if vertices <tt>v</tt> and <tt>w</tt> are in the same
     *     strong component, and <tt>false</tt> otherwise
     */
    public boolean stronglyConnected(int v, int w) 
    {
        return compIdOfVertex[v] == compIdOfVertex[w];
    }
    
    public boolean stronglyConnected(PartitionVertex v, PartitionVertex w) 
    {
        return stronglyConnected(v.LocalId, w.LocalId);
    }

    /**
     * Returns the component id of the strong component containing vertex <tt>v</tt>.
     * @param v the vertex
     * @return the component id of the strong component containing vertex <tt>v</tt>
     */
    public int getHostingComponent(int v)
    {
        return compIdOfVertex[v];
    }
    
    public int getHostingComponent(PartitionVertex v)
    {
        return getHostingComponent(v.LocalId);
    }
    
    public void printVertexAssignments()
    {
        for(PartitionVertex vertex : vertexes)
        {
        	System.out.println("Component(" + vertex.getId() + ") = " + compIdOfVertex[vertex.LocalId]);
        }
    }
    
    

    
	
	public static void main(String[] args) throws Exception
	{
		if(args.length > 0)
		{
			Helper.setParametersFromFile(args[0]);
			Helper.printParamValues();
		}
		//read a graph file
		System.out.println("Reading the graph...");
		//String graphFileName = args[0];
		//Graph graph = GraphFileIO.loadGraph(graphFileName, true);
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		//partition the graph by EDP partitioner
		System.out.println("Partitioning the graph...");
		EdgeDisjointIndex edp = EdgeDisjointIndex.buildIndex(graph, null);
		int numOfPartitions = edp.getNumOfPartitions();
		System.out.println("Number of Partitions: " + numOfPartitions);
		System.out.println("Computing the CCs...");
		ConnectedComponentsComputation[] components = new ConnectedComponentsComputation[numOfPartitions];
		for(int i = 0; i < numOfPartitions; i++)
		{
			components[i] = new ConnectedComponentsComputation();
			components[i].buildSCC(edp.getPartition(i));
			Partition partition = edp.getPartition(i);
			int numOfConnectedComponents = partition.ConnectedComponents.getConnectedComponentsCount();
			System.out.println("\t Partition " + i + " with " + edp.getPartition(i).vertexCount() +" vertexes has: " + components[i].NumOfComponents() + " components.");
			System.out.println("\t Partition " + i + " with " + edp.getPartition(i).vertexCount() +" vertexes has: " + edp.getPartition(i).ConnectedComponents.getConnectedComponentsCount() + " components.");
			ArrayList<ArrayList<Integer>> componentsVertexes = new ArrayList<ArrayList<Integer>>(partition.ConnectedComponents.getConnectedComponentsCount());
			for(int j = 0; j < numOfConnectedComponents; j++)
			{
				componentsVertexes.add(new ArrayList<Integer>());
			}
			for(PartitionVertex vertex : partition.getAllVertexes())
			{
				componentsVertexes.get(vertex.ComponentId).add(vertex.getId());
			}
			for(int j = 0; j < numOfConnectedComponents; j++)
			{
				System.out.println(componentsVertexes.get(j));
			}
		}
		
		
		
		
		//for each partition, check that its SCC vertexes are disjoint and makes the final count
	}

}
