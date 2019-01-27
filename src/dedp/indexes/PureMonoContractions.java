package dedp.indexes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dedp.algorithms.FloydWarshal;
import dedp.common.BytesValue;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.io.GraphFileIO;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.Vertex;


//pure here means without a hybrid partition
public class PureMonoContractions 
{
	public static DisjointLabelsIndex buildPureMonoIndex(Graph graph) throws DuplicateEntryException, ObjectNotFoundException, IOException
	{
		DisjointLabelsIndex index = new DisjointLabelsIndex();
		index.IndexGraph = new Graph();
		int numOfAllLabels = graph.Labels.size();
		index.HybridLabel = -1; //this property is not used
		index.NumberOfLabels = numOfAllLabels;
		Graph[] monoGraphs = new Graph[numOfAllLabels];
		Graph[] monoContractions = new Graph[numOfAllLabels];
		
		for(int i = 0; i < monoGraphs.length; i++)
		{
			monoGraphs[i] = new Graph();
		}
		for(int i = 0; i < monoContractions.length; i++)
		{
			monoContractions[i] = new Graph();
		}
		//scan the big graph and build the monographs
		Collection<Vertex> allVertexes = graph.getAllVertexes();
		//map from vertex to its label, note multi-colored label = (numOfAllLabels - 1)
		HashMap<Long, Integer> vertexToLabel = new HashMap<Long, Integer>();
		Edge[] outEdges = null;
		int label = -1;
		//set the labels of all the vertexes
		for(Vertex v : allVertexes)
		{
			Map<Integer, Integer> colorCount = new HashMap<Integer, Integer>(); //key = color, value = number of out edges colored by key
			int dominantColorCount = -1;
			label = -1;
			if(v.fanIn() == 0 && v.fanOut() == 0)
			{
				label = index.NumberOfLabels - 1;
			}
			else //count the number of in and out colors
			{
				for(Edge e : v.getInEdges())
				{
					if(!colorCount.containsKey(e.getLabel()))
					{
						colorCount.put(e.getLabel(), 1);
						if(dominantColorCount < 1)
						{
							dominantColorCount = 1;
							label = e.getLabel();
						}
					}
					else
					{
						int count = colorCount.get(e.getLabel());
						colorCount.put(e.getLabel(), count + 1);
						count = colorCount.get(e.getLabel());
						if(dominantColorCount < count)
						{
							dominantColorCount = count;
							label = e.getLabel();
						}
					}
				}
				
				for(Edge e : v.getOutEdges())
				{
					if(!colorCount.containsKey(e.getLabel()))
					{
						colorCount.put(e.getLabel(), 1);
						if(dominantColorCount < 1)
						{
							dominantColorCount = 1;
							label = e.getLabel();
						}
					}
					else
					{
						int count = colorCount.get(e.getLabel());
						colorCount.put(e.getLabel(), count + 1);
						count = colorCount.get(e.getLabel());
						if(dominantColorCount < count)
						{
							dominantColorCount = count;
							label = e.getLabel();
						}
					}
				}
			}
			v.Label = label;
			monoGraphs[label].addVertex(v.getID()).Label = label;
			vertexToLabel.put(v.getID(), label);
		}
		if(Constants.Debug)
		{
			System.out.println("Vertex label assignment");
			for(Vertex v : allVertexes)
			{
				System.out.println("\t ID: " + v.getID() + ", Color: " + v.Label);
			}
		}
		
		
		//add the edges to each mono colored graph
		for(int i = 0; i < monoGraphs.length; i++)
		{
			for(Vertex v : monoGraphs[i].getAllVertexes())
			{
				for(Edge e : graph.getVertex(v.getID()).getOutEdges())
				{
					if (e.getFrom().Label == i && e.getTo().Label == i && e.getLabel() == i)
					{
						//make sure we didn't already add the edge
						if(!monoGraphs[i].containsEdge(e.getID()))
						{
							monoGraphs[i].addEdge(e.getID(), e.getFrom().getID(), e.getTo().getID(), e.getWeight(), e.getLabel(), true, false);
						}
					}
				}
			}
		}
		
		
		//contract the monographs
		//String contractedGraphFileNameBase = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\dc_directed\\index_pure\\graph";
		String contractedGraphFileNameBase = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\index2\\graph";
		Graph contractedGraph = null;
		for(int i = 0; i < monoGraphs.length; i++)
		{
			//read graphs from file
			
			//contractedGraph = GraphFileIO.loadGraph(contractedGraphFileNameBase + i + ".csv", true, false);
			
			contractedGraph = FloydWarshal.ComputeSPContraction(monoGraphs[i], i);
			GraphFileIO.saveGraph(contractedGraph, contractedGraphFileNameBase + i + ".csv", false);
			
			for(Edge e : contractedGraph.getAllEdges())
			{
				BytesValue bytes = Helper.getEdgeBytesRepresentative(e.getFrom().getID(), e.getTo().getID());
				if(!index.distanceMap.containsKey(bytes))
				{
					index.distanceMap.put(bytes, e.getWeight());
				}
			}
			
			for(Vertex v : monoGraphs[i].getAllVertexes())
			{
				if(!contractedGraph.containsVertex(v.getID()))
				{
					contractedGraph.addVertex(v.getID());
				}
			}
			
			for(Vertex v : contractedGraph.getAllVertexes())
			{
				v.Label = i;
			}
			
			monoGraphs[i] = contractedGraph;
			
			//save the graph into a file
		}
		//combine the separate mono color graphs
		for(int i = 0; i < monoGraphs.length; i++)
		{
			for(Vertex v : monoGraphs[i].getAllVertexes())
			{
				index.IndexGraph.addVertex(v);
			}
		}
		int edgeID = 1;
		for(int i = 0; i < monoGraphs.length; i++)
		{
			for(Edge e : monoGraphs[i].getAllEdges())
			{
				e.setID(edgeID++); //assign new IDs
				index.IndexGraph.addEdge(e);
			}
		}
		
		//add the bridge edges
		for(Edge e : graph.getAllEdges())
		{
			int labelFrom = e.getFrom().Label;
			int labelTo = e.getTo().Label;
			if(labelFrom != labelTo)
			{
				//if(!index.containsEdge(e.getID()))
				{
					index.IndexGraph.addEdge(edgeID++, e.getFrom().getID(), e.getTo().getID(), e.getWeight(), e.getLabel(), true, false);
				}
			}
		}
		
		//set the on brdige edges
		for(Edge e : index.IndexGraph.getAllEdges())
		{
			if(e.getFrom().Label != e.getTo().Label)
			{
				e.getFrom().onBridge = true;
			}
		}
		
		for(Edge e : index.IndexGraph.getAllEdges())
		{
			if(e.getFrom().Label == e.getTo().Label &&  e.getTo().onBridge)
			{
				if(!e.getFrom().beforeBridgeEdges.contains(e))
				{
					e.getFrom().beforeBridgeEdges.add(e);
				}
			}
			
			
			if(e.getFrom().Label != e.getTo().Label) //i.e from is a "bridge vertex"
			{
				if(!e.getFrom().bridgeEdges.contains(e))
				{
					e.getFrom().bridgeEdges.add(e);
				}
			}
			
		}
		
		/*
		int vCount = 0, eCount = 0;
		for(int i = 0; i < numOfAllLabels; i++)
		{
			System.out.println(i + ": " + monoGraphs[i].getVertexCount() + ", " + monoGraphs[i].getEdgeCount());
			vCount += monoGraphs[i].getVertexCount();
			eCount += monoGraphs[i].getEdgeCount();
		}
		System.out.println("s(vertexes): " + vCount + ", s(edges): " + eCount);
		System.out.println("s(Index.vertexes): " + index.getVertexCount() + ", s(Index.edges): " + index.getEdgeCount());
		*/
		return index;
	}
	
	public static void main(String[] args) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException 
	{
		Graph graph = GraphFileIO.loadGraph(Constants.GraphFileName, true);
		System.out.println("Building the index...");
		DisjointLabelsIndex index = buildPureMonoIndex(graph);
		//mark the onBridge edges
		int[] labelCount = new int[index.NumberOfLabels];
		int[] bridgeCount = new int[index.NumberOfLabels];
		for(int i = 0; i < labelCount.length; i++)
		{
			labelCount[i] = 0;
			bridgeCount[i] = 0;
		}
		/*
		for(Edge e : index.IndexGraph.getAllEdges())
		{
			if(e.getFrom().Label < index.HybridLabel && e.getTo().Label == index.HybridLabel)
			{
				e.getFrom().onBridge = true;
				if(!e.getFrom().beforeBridgeEdges.contains(e))
				{
					e.getFrom().beforeBridgeEdges.add(e);
				}
			}
			
			if(e.getTo().Label < index.HybridLabel && e.getFrom().Label == index.HybridLabel)
			{
				e.getTo().onBridge = true;
			}
		}
		*/
		for(Vertex v : index.IndexGraph.getAllVertexes())
		{
			labelCount[v.Label]++;
			if(v.onBridge)
			{
				bridgeCount[v.Label]++;
			}
		}
		System.out.println("Original vertex count: " + graph.getVertexCount());
		System.out.println("Original edge count: " + graph.getEdgeCount());
		System.out.println("Index vertex count: " + index.IndexGraph.getVertexCount());
		System.out.println("Index edge count: " + index.IndexGraph.getEdgeCount());
		
		//System.out.println("Vertex count: " + index.IndexGraph.getVertexCount());
		System.out.println("Label\tLabelCount\tBridgeCount");
		for(int i = 0; i < labelCount.length; i++)
		{
			System.out.println(i + "\t" + labelCount[i] + "\t" + bridgeCount[i]);
		}
		
		int maxFanout = 0;
		int maxBeforeEdges = 0;
		int maxBridgeEdges = 0;
		for(Vertex v : index.IndexGraph.getAllVertexes())
		{
			if(v.getOutEdges().size() > maxFanout)
			{
				maxFanout = v.getOutEdges().size();
			}
			
			if(v.beforeBridgeEdges.size() > maxBeforeEdges)
			{
				maxBeforeEdges = v.beforeBridgeEdges.size();
			}
			
			if(v.bridgeEdges.size() > maxBridgeEdges)
			{
				maxBridgeEdges = v.beforeBridgeEdges.size();
			}
			
		}
		System.out.println("Max fan out for any node " + maxFanout);
		System.out.println("Max beforeBrideEdges " + maxBeforeEdges);
		System.out.println("Max brideEdges " + maxBridgeEdges);
		//System.out.println("Diatance entries " + index.distanceMap.size());
		
		/*
		System.out.println("Original vertex count: " + graph.getVertexCount());
		System.out.println("Original edge count: " + graph.getEdgeCount());
		System.out.println("Index vertex count: " + index.getVertexCount());
		System.out.println("Index edge count: " + index.getEdgeCount());
		*/
	}
	
}
