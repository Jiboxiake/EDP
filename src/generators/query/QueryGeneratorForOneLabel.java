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

public class QueryGeneratorForOneLabel 
{
	public static void main(String[] args) throws Exception
	{
		if(args.length > 0)
		{
			Helper.setParametersFromFile(args[0]);
			Helper.printParamValues();
		}
		//read the graph
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		//labelVertexes[i] will contain all the vertexes with outgoing edge of label i
		int numOfLabels = graph.LabelsIDs.size();
		ArrayList<ArrayList<Vertex>> labelVertexes = new ArrayList<ArrayList<Vertex>>();
		Random rnd = new Random(1234567);
		for(int i = 0; i < 2000; i++)
		{
			rnd.nextInt();
		}
		for(int i = 0; i < numOfLabels; i++)
		{
			labelVertexes.add(new ArrayList<Vertex>());
		}
		for(Vertex v : graph.getAllVertexes())
		{
			for(Edge e : v.getOutEdges())
			{
				 int label = e.getLabel();
				 if(!labelVertexes.get(label).contains(v))
				 {
					 labelVertexes.get(label).add(v);
				 }
			}
		}
		//report number of vertexes at each bucket
		for(int i = 0; i < numOfLabels; i++)
		{
			System.out.println("Label " + i + " has vertexes of size: " + labelVertexes.get(i).size());
		}
		//create the queries and write each file
		ArrayList<Query> lstQueries = new ArrayList<Query>(Constants.NumOfQueries);
		ArrayList<Integer> lstLabels = new ArrayList<Integer>();
		
		//for(int label = 0; label < numOfLabels; label++)
		for(int label = 0; label < 1; label++)
		{
			System.out.println("Generating queries for label " + label + ".");
			int totalAddedQueries = 0;
			int lastChunkCount = 0;
			while(totalAddedQueries < Constants.NumOfQueries)
			{
				int randomSource = rnd.nextInt(labelVertexes.get(label).size());
				int source = (int)labelVertexes.get(label).get(randomSource).getID();
				lstLabels.clear();
				lstLabels.add(label);
				lastChunkCount = Query.shortestDistance(graph, source, lstLabels, Constants.MinHops, Constants.NumOfQueries - totalAddedQueries, lstQueries);
				totalAddedQueries += lastChunkCount;
				//lstQueries = Query.shortestDistance(graph, source, lstLabels, Constants.MinHops, Constants.NumOfQueries);
			}
			QueryIO.writeQueryFile(lstQueries, getQueryFileName(label), true, true);
		}
	}
	
	public static String getQueryFileName(int labelId)
	{
		return Constants.QueryFileBaseName + "_" + labelId + ".csv";
	}
	public static String getQueryFileName()
	{
		return Constants.QueryFileBaseName + "_Mix" + ".csv";
	}
}
