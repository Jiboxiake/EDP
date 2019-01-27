import java.io.IOException;
import java.util.ArrayList;

import dedp.algorithms.hybridtraversal.HybridTraversal;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.io.GraphFileIO;
import dedp.structures.Graph;
import dedp.structures.SPResult;

public class Test {

	public static void main(String[] args) throws Exception {
		
		// Set global parameters, mainly to set the directories on which the index entries will be stored
		Helper.setParametersFromFile("params.txt");
		
		// Build the graph from text file
		//(String edgesFilePath, boolean isDirected, boolean firstRowIsHeader, int numOfV, int numOfE, int numOfL, int expFanOut) 
		Graph graph = GraphFileIO.loadGraph("graph.csv", true, true, 1000, 1000, 40, 5);
		
		// Construct the index
		ArrayList<Integer> excludedPartitions = new ArrayList<Integer>(); //in case you need to exlcude some partitions
		System.out.println("Start index initialization...");
		EdgeDisjointIndex index = EdgeDisjointIndex.buildIndex(graph, excludedPartitions);
		System.out.println("Index has been initialized...entries will be added according to the workload");
		
		// Run a query
		int from = 0, to = 2;
		ArrayList<Integer> allowedLabels = new ArrayList<Integer>();
		allowedLabels.add(0);
		//allowedLabels.add(1);
		SPResult spResult = HybridTraversal.shortestDistanceWithEdgeDisjointIndex(index, from, to, allowedLabels);
		System.out.println("Shortest distance = " + spResult.Distance);
	}

}
