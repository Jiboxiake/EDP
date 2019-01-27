package dedp.algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import dedp.algorithms.hybridtraversal.EdgeDisjointQueueEntry;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.io.GraphFileIO;
import dedp.structures.Graph;

public class TarjanSCC 
{

	
	private boolean[] marked;        // marked[v] = has v been visited?
    private int[] id;                // id[v] = id of strong component containing v
    private int[] low;               // low[v] = low number of v
    private int pre;                 // preorder number counter
    private int count;               // number of strongly-connected components
    private Stack<Integer> stack;
    private ArrayList<PartitionVertex> vertexes;
    private int numOfVertexes;
    
    public int buildSCC(Partition partition) 
    {
    	numOfVertexes = partition.vertexCount();
    	vertexes = new ArrayList<PartitionVertex>(partition.getAllVertexes());
        marked = new boolean[numOfVertexes];
        stack = new Stack<Integer>();
        id = new int[numOfVertexes]; 
        low = new int[numOfVertexes];
        for(int v = 0; v < numOfVertexes; v++)
        {
        	vertexes.get(v).LocalId = v;
        }
        for (int v = 0; v < numOfVertexes; v++) {
            if (!marked[v]) dfs(vertexes.get(v));
        }
        return this.CountOfSCC();
        // check that id[] gives strong components
        //assert check(G);
    }
    
    private void dfs(PartitionVertex v) 
    { 
        marked[v.LocalId] = true;
        low[v.LocalId] = pre++;
        int min = low[v.LocalId];
        stack.push(v.LocalId);
        PartitionVertex w;
        for (PartitionEdge e : v.getOutEdges())
        {
        	w = e.getTo();
            if (!marked[w.LocalId]) dfs(w);
            if (low[w.LocalId] < min) min = low[w.LocalId];
        }
        if (min < low[v.LocalId]) { low[v.LocalId] = min; return; }
        int wId;
        do {
            wId = stack.pop();
            id[wId] = count;
            low[wId] = numOfVertexes;
        } while (wId != v.LocalId);
        count++;
    }
    
    /**
     * Returns the number of strong components.
     * @return the number of strong components
     */
    public int CountOfSCC() 
    {
        return count;
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
        return id[v] == id[w];
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
        return id[v];
    }
    
    public int getHostingComponent(PartitionVertex v)
    {
        return id[v.LocalId];
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
		//for each partition, construct its SCC, and print its count
		System.out.println("Computing the SCCs...");
		TarjanSCC[] components = new TarjanSCC[numOfPartitions];
		for(int i = 0; i < numOfPartitions; i++)
		{
			components[i] = new TarjanSCC();
			components[i].buildSCC(edp.getPartition(i));
			System.out.println("\t Partition " + i + " with " + edp.getPartition(i).vertexCount() +" vertexes has: " + components[i].CountOfSCC() + " components.");
		}
		
		
		
		//for each partition, check that its SCC vertexes are disjoint and makes the final count
	}

}
