package dedp.transformers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.io.GraphFileIO;
import dedp.structures.Graph;

public class TigerToNativeFormat 
{
	public static void main(String[] args) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException 
	{
		//read the tiger file, create a memory graph, write the memory graph
		String inputFile = args[0];
		String outputFile = args[1];
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line = "";
		String[] tempArr = null;
		long numOfVertexes = -1, numOfEdges = -1;
		Graph graph = new Graph();
		if((line = reader.readLine()) != null)
		{
			//read the number of vertexes
			numOfVertexes = Long.parseLong(line);
			for(long i = 0; i < numOfVertexes; i++)
			{
				line = reader.readLine();
				tempArr = line.split(" ");
				graph.addVertex(Long.parseLong(tempArr[0]));
			}
			//read the number of edges
			line = reader.readLine();
			numOfEdges = Long.parseLong(line);
			long from, to;
			float distance;
			String label;
			for(long i = 0; i < numOfEdges; i++)
			{
				//format is "from to"
				line = reader.readLine();
				tempArr = line.split(" ");
				from = Long.parseLong(tempArr[0]);
				to = Long.parseLong(tempArr[1]);
				//format is "traveltime distanceinmeters roadcategory"
				line = reader.readLine();
				tempArr = line.split(" ");
				distance = Float.parseFloat(tempArr[1]);
				label = tempArr[2].trim().toLowerCase();
				graph.addEdge(i, from, to, distance, label, false, false);
				tempArr = null; line = null;
			}
		}
		//print some statistics
		System.out.println("#input vertexes: " + numOfVertexes);
		System.out.println("#input edges: " + numOfEdges);
		System.out.println("#Graph vertexes: " + graph.getVertexCount());
		System.out.println("#Graph edges: " + graph.getEdgeCount());
		System.out.println("#Graph Labels: " + graph.Labels.size());
		for(Map.Entry<String, Integer> entry : graph.Labels.entrySet())
		{
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}
		GraphFileIO.saveGraph(graph, outputFile, true);
	}
}
