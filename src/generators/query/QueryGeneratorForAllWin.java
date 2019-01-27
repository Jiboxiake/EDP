package generators.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.io.GraphFileIO;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.Vertex;

public class QueryGeneratorForAllWin 
{
	public static void main(String[] args) throws Exception
	{
		if(args.length > 0)
		{
			Helper.setParametersFromFile(args[0]);
			Helper.printParamValues();
		}
		System.out.println("Start reading graph...");
		//read the graph
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		System.out.println("The graph was read...");
		//labelVertexes[i] will contain all the vertexes with outgoing edge of label i
		int numOfLabels = graph.LabelsIDs.size();
		long numOfVertexes = graph.getVertexCount();
		long numOfEdges = graph.getEdgeCount();
		Random rnd = new Random(1234567);
		for(int i = 0; i < 2000; i++)
		{
			rnd.nextInt();
		}
		ArrayList<Vertex> lstVertexes = new ArrayList<Vertex>(50000);
		for(Vertex v : graph.getAllVertexes())
		{
			for(Edge e : v.getOutEdges())
			{
				 int label = e.getLabel();
				 if(label == 0 && !lstVertexes.contains(v))
				 {
					 lstVertexes.add(v);
				 }
			}
			if(lstVertexes.size() == 50000)
				break;
		}
		System.out.println("Veretexe were selected...");
		//create the queries and write each file
		ArrayList<Query> lstQueries = new ArrayList<Query>(Constants.NumOfQueries);
		ArrayList<Integer> lstLabels = new ArrayList<Integer>();
		lstLabels.add(0);
		//for(int label = 0; label < numOfLabels; label++)
		int numOfVertexesInBucket = lstVertexes.size();
		for(int label = 0; label < 1; label++)
		{
			System.out.println("Generating queries for label " + label + ".");
			int totalAddedQueries = 0;
			int lastChunkCount = 0;
			while(totalAddedQueries < Constants.NumOfQueries)
			{
				int randomSource = rnd.nextInt(numOfVertexesInBucket);
				int source = (int)lstVertexes.get(randomSource).getID();
				lastChunkCount = Query.shortestDistance(graph, source, lstLabels, Constants.MinHops, Constants.NumOfQueries - totalAddedQueries, lstQueries);
				totalAddedQueries += lastChunkCount;
				//lstQueries = Query.shortestDistance(graph, source, lstLabels, Constants.MinHops, Constants.NumOfQueries);
			}
			QueryIO.writeQueryFile(lstQueries, getQueryFileName(label), true, false);
		}
	}
	
	public static String getQueryFileName(int labelId)
	{
		return Constants.QueryFileBaseName + "_" + labelId + "_Sketch.csv";
	}
	public static String getQueryFileName()
	{
		return Constants.QueryFileBaseName + "_Mix" + ".csv";
	}
}
