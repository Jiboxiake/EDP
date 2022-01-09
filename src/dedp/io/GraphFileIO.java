package dedp.io;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dedp.common.Constants;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.structures.*;

public class GraphFileIO 
{
	
	public static Graph loadGraph(String edgesFilePath, boolean isDirected) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		return loadGraph(edgesFilePath, isDirected, true);
	}
	
	public static Graph loadGraph(String edgesFilePath, boolean isDirected, boolean firstRowIsHeader) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		return loadGraph(edgesFilePath, isDirected, firstRowIsHeader, 240000, 22500000, 49, 200);
	}
	
	public static Graph loadGraph(String edgesFilePath, boolean isDirected, boolean firstRowIsHeader, int numOfV, int numOfE, int numOfL, int expFanOut) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		Graph graph = new Graph(numOfV, numOfE, numOfL, expFanOut);
		//read edges and add vertexes automatically
		BufferedReader eReader = new BufferedReader(new FileReader(edgesFilePath));
		Edge e = null;
		String line = "";
		if(firstRowIsHeader)
		{
			eReader.readLine(); //skip header
		}
		String[] temp = null;
		int lastLabelID = 0;
		int labelID = -1;
		float weight = 1f;
		//edgeID, from, to, label, [weight]
		int numOfEdges = 0;
		while ((line = eReader.readLine()) != null) 
		{
			temp = line.split(",");
			temp[3] = temp[3].trim();
			if(graph.Labels.containsKey(temp[3]))
			{
				labelID = graph.Labels.get(temp[3]);
			}
			else
			{
				graph.Labels.put(temp[3], lastLabelID);
				labelID = lastLabelID;
				lastLabelID++;
			}
			if(temp.length > 4)
			{
				weight = Float.parseFloat(temp[4]);
			}
			else
			{
				weight = 1f;
			}
			graph.addEdge(Long.parseLong(temp[0]), Long.parseLong(temp[1]), Long.parseLong(temp[2]), weight, labelID, isDirected);
			numOfEdges++;
			if(numOfEdges % 20000 == 0)
			{
				System.out.println(numOfEdges + " edge processed.");
			}
		}
		eReader.close();
		return graph;
	}
	//TODO: rewrite loadPartition
	public static Partition loadPartition(int label, String edgesFilePath, boolean isDirected, boolean firstRowIsHeader) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		Partition partition = new Partition(label);
		//read edges and add vertexes automatically
		BufferedReader eReader = new BufferedReader(new FileReader(edgesFilePath));
		Edge e = null;
		String line = "";
		if(firstRowIsHeader)
		{
			eReader.readLine(); //skip header
		}
		String[] temp = null;
		int lastLabelID = 0;
		float weight = 1f;
		//edgeID, from, to, label, [weight]
		while ((line = eReader.readLine()) != null) 
		{
			temp = line.split(",");
			temp[3] = temp[3].trim();
			if(temp.length > 4)
			{
				weight = Float.parseFloat(temp[4]);
			}
			else
			{
				weight = 1f;
			}
			partition.addEdge(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), weight, label, isDirected, new ArrayList<Integer>(), new ArrayList<Integer>());
		}
		eReader.close();
		return partition;
	}
	
	
	public static void saveGraph(Graph graph, String graphFileName, boolean writeHeader) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		//read edges and add vertexes automatically
		BufferedWriter eWriter = new BufferedWriter(new FileWriter(graphFileName));
		String line = "";
		if(writeHeader)
		{
			eWriter.write( "edgeID,from,to,label,weight\n");
		}
		//edgeID, from, to, label, [weight]
		for(Edge e : graph.getAllEdges())
		{
			line = e.getID() + "," + e.getFrom().getID() + "," + e.getTo().getID() + "," + e.getLabel()  + "," + e.getWeight() + "\n";
			eWriter.write(line);
		}
		eWriter.close();
	}
	
	public static void savePartition(Partition partition, String graphFileName, boolean writeHeader) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		//read edges and add vertexes automatically
		BufferedWriter eWriter = new BufferedWriter(new FileWriter(graphFileName));
		String line = "";
		if(writeHeader)
		{
			eWriter.write( "edgeID,from,to,label,weight\n");
		}
		//edgeID, from, to, label, [weight]
		for(PartitionEdge e : partition.getAllEdges())
		{
			line = e.getId() + "," + e.getFrom().getId() + "," + e.getTo().getId() + "," + e.getLabel()  + "," + e.getWeight() + "\n";
			eWriter.write(line);
		}
		eWriter.close();
	}
	
	
	public static void printGraph(Graph graph)
	{
		//print the vertexes
		for(Vertex v : graph.getAllVertexes())
		{
			System.out.println("ID: " + v.getID() + ", Color: " + v.Label + ", OnBridge: " + v.onBridge);
			System.out.println("\tOut edges" );
			for(Edge e: v.getOutEdges())
			{
				System.out.println("\t\t --> " + e.getTo().getID() + ", Color: " + e.getLabel() + ", Weight: " + e.getWeight() );
			}
			System.out.println("\tBefore bridge edges" );
			for(Edge e: v.beforeBridgeEdges)
			{
				System.out.println("\t\t --> " + e.getTo().getID() + ", Color: " + e.getLabel() + ", Weight: " + e.getWeight() );
			}
			System.out.println("\tBridge edges" );
			for(Edge e: v.bridgeEdges)
			{
				System.out.println("\t\t --> " + e.getTo().getID() + ", Color: " + e.getLabel() + ", Weight: " + e.getWeight() );
			}
			
		}
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException, DuplicateEntryException, ObjectNotFoundException 
	{
		// TODO Auto-generated method stub
		List<Integer> labelIDs = new ArrayList<Integer>();
		labelIDs.add(0);
		labelIDs.add(1);
		labelIDs.add(2);
		String fileName = args[0];
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		print(graph);
		//float distance = shortestDistance(graph, 42390684, 42391980, labelIDs);
		//System.out.print("Distance is " + distance);
		
	}
	
	public static void print(Graph graph)
	{
		int numOfLabels = graph.Labels.size();
		System.out.println("Number of vertexes: " + graph.getVertexCount());
		System.out.println("Number of edges: " + graph.getEdgeCount());
		System.out.println("Number of labels: " + numOfLabels);
		long maxFanOut = -1, maxDiversity, totalFanOut = 0, totalDiversity, numOfBridgeEdges, minFanOut = Long.MAX_VALUE;
		long fanOut = 0;
		long countOfNonMonoVertexes = 0, sumOfFanOutOfNonMonoVertexes = 0;
		long numOfBridgeVerexes = 0;
		List<Integer> lst = new ArrayList<Integer>(10);
		int vFanOut = 0;
		for(Vertex v : graph.getAllVertexes())
		{
			fanOut = v.fanOut();
			if(fanOut > maxFanOut)
				maxFanOut = fanOut;
			
			lst.clear();
			vFanOut = v.fanOut();
			//count bridge veretxes
			for(int i = 0; i < vFanOut; i++)
			{
				if (!lst.contains(v.outEdges.get(i).getLabel()))
				{
					lst.add(v.outEdges.get(i).getLabel());
				}
			}
			if(lst.size() > 0)
			{
				numOfBridgeVerexes += (lst.size() - 1);
			}
			
			for(int i = 1; i < vFanOut; i++)
			{
				if (v.outEdges.get(0).getLabel() != v.outEdges.get(i).getLabel())
				{
					countOfNonMonoVertexes++;
					sumOfFanOutOfNonMonoVertexes += vFanOut;
					break;
				}
			}
			
			if(fanOut < minFanOut)
				minFanOut = fanOut;
			
			totalFanOut += fanOut;
		}
		double avgFanOut = (double)totalFanOut / (double)graph.getVertexCount();
		double avgNonMonoFanOut = (double)sumOfFanOutOfNonMonoVertexes / (double)countOfNonMonoVertexes;
		System.out.println("MinFanOut: " + minFanOut);
		System.out.println("MaxFanOut: " + maxFanOut);
		System.out.println("AvgFanOut: " + avgFanOut);
		System.out.println("AvgNonMonoFanOut: " + avgNonMonoFanOut);
		System.out.println("CountOfNonMonoVertexes: " + countOfNonMonoVertexes);
		System.out.println("NumOfBridgeVertexes: " + numOfBridgeVerexes);
		int[] counts = new int[numOfLabels];
		for(int i = 0; i < numOfLabels; i++)
		{
			counts[i] = 0;
		}
		Collection<Edge> edges = graph.getAllEdges();
		for(Edge edge : edges)
		{
			counts[edge.getLabel()]++;
		}
		for(int i = 0; i < numOfLabels; i++)
		{
			System.out.println("Label " + i + ": " + counts[i]);
		}
	}

}
