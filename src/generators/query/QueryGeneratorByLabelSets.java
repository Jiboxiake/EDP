package generators.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

public class QueryGeneratorByLabelSets 
{
	public static void main(String[] args) throws Exception
	{
		if(args.length > 0)
		{
			Helper.setParametersFromFile(args[0]);
			Helper.printParamValues();
		}
		System.out.println("Reading the graph...");
		//read the graph
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		//labelVertexes[i] will contain all the vertexes with outgoing edge of label i
		int numOfLabels = graph.LabelsIDs.size();
		ArrayList<ArrayList<Integer>> labelVertexes = new ArrayList<ArrayList<Integer>>();
		Random rnd = new Random(1234567);
		for(int i = 0; i < 2000; i++)
		{
			rnd.nextInt();
		}
		for(int i = 0; i < numOfLabels; i++)
		{
			labelVertexes.add(new ArrayList<Integer>((int)graph.getVertexCount()));
		}
		System.out.println("Computing cardenalities...");
		int vId = 0, label = 0;
		Collection<Vertex> vertexes = graph.getAllVertexes();
		for(Vertex v : vertexes)
		{
			for(Edge e : v.getOutEdges())
			{
				//for performance, it will not harm if we add an element twice
				 //label = e.getLabel();
				 //vId = (int)v.getID();
				 //if(!labelVertexes.get(label).contains(vId))
				 {
					 //labelVertexes.get(label).add(vId);
					 labelVertexes.get(e.getLabel()).add((int)v.getID());
				 }
			}
		}
		//report number of vertexes at each bucket
		for(int i = 0; i < numOfLabels; i++)
		{
			System.out.println("Label " + i + " has vertexes of size: " + labelVertexes.get(i).size());
		}
		//create the queries and write each file
		ArrayList<Query> lstQueries = new ArrayList<Query>();
		
		for(int lblSize = 1; lblSize <= numOfLabels; lblSize++)
		{
			System.out.println("Generating queries of labels " + lblSize + ".");
			for(int lbl = 0; lbl < numOfLabels; lbl++)
			{
				for(int i = 0; i < Constants.NumOfRandomSources; i++)
				{
					int randomSource = rnd.nextInt(labelVertexes.get(lbl).size());
					int source = labelVertexes.get(lbl).get(randomSource);
					
					lstQueries.addAll(Query.shortestDistance(graph, source, lblSize, Constants.MinHops, Constants.NumOfQueries));
					
				}
			}
		}
		QueryIO.writeQueryFile(lstQueries, QueryGenerator.getQueryFileName(), true, true);
	}
	
	
}
