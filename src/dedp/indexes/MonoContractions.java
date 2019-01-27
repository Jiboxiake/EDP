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

public class MonoContractions 
{
	public static DisjointLabelsIndex buildIndex(Graph graph) throws DuplicateEntryException, ObjectNotFoundException, IOException
	{
		DisjointLabelsIndex index = new DisjointLabelsIndex();
		index.IndexGraph = new Graph();
		int numOfAllLabels = graph.Labels.size() + 1;
		int hybridLabel = numOfAllLabels - 1;
		index.HybridLabel = hybridLabel;
		index.NumberOfLabels = numOfAllLabels;
		Graph[] monoGraphs = new Graph[numOfAllLabels];
		Graph[] monoContractions = new Graph[numOfAllLabels - 1];
		
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
		int label = -1;
		//set the labels of all the vertexes
		for(Vertex v : allVertexes)
		{
			boolean monoColored = true;
			label = hybridLabel;
			if(v.fanIn() == 0 && v.fanOut() == 0)
			{
				monoColored = false;
			}
			else if (v.fanIn() == 0 && v.fanOut() > 0)
			{
				for(int i = 1; i < v.fanOut(); i++)
				{
					if(v.getOutEdges().get(0).getLabel() != v.getOutEdges().get(i).getLabel())
					{
						monoColored = false;
						break;
					}
				}
				if(monoColored)
				{
					label = v.getOutEdges().get(0).getLabel();
				}
			}
			else if (v.fanIn() > 0 && v.fanOut() == 0)
			{
				for(int i = 1; i < v.fanIn(); i++)
				{
					if(v.getInEdges().get(0).getLabel() != v.getInEdges().get(i).getLabel())
					{
						monoColored = false;
						break;
					}
				}
				if(monoColored)
				{
					label = v.getInEdges().get(0).getLabel();
				}
			}
			else //fan out > 0 and fan in > 0
			{
				for(int i = 1; i < v.fanOut(); i++)
				{
					if(v.getOutEdges().get(0).getLabel() != v.getOutEdges().get(i).getLabel())
					{
						monoColored = false;
						break;
					}
				}
				if(monoColored)
				{
					int lblOfAllFanOut = v.getOutEdges().get(0).getLabel();
					for(int i = 1; i < v.fanIn(); i++)
					{
						if(v.getInEdges().get(0).getLabel() != v.getInEdges().get(i).getLabel())
						{
							monoColored = false;
							break;
						}
					}
					if(monoColored && (lblOfAllFanOut == v.getInEdges().get(0).getLabel()))
					{
						label = lblOfAllFanOut;
					}
					else
					{
						monoColored = false;
					}
				}
			}
			if(!monoColored)
			{
				label = hybridLabel;
			}
			v.Label = label;
			monoGraphs[label].addVertex(v.getID()).Label = label;
			//monoGraphs[label].addVertex(v);
			vertexToLabel.put(v.getID(), label);
		}
		/*
		//now iterate over the hybrid vertexes to reassign them to mono partitions if possible
		List<Long> idsToRemoveFromHybrid = new ArrayList<Long>();
		for(Vertex v : monoGraphs[hybridLabel].getAllVertexes())
		{
			LinkedList<Edge> inEdges = v.getInEdges(); 
			boolean toBeMono = (inEdges.size() > 0 && inEdges.get(0).getFrom().Label < hybridLabel);
			for(int i = 0; toBeMono && (i < inEdges.size()); i++)
			{
				if(!((inEdges.get(i).getLabel() < hybridLabel)
						&& (inEdges.get(0).getLabel() == inEdges.get(0).getFrom().Label) 
						&& (inEdges.get(i).getLabel() == inEdges.get(0).getLabel())
						&& (inEdges.get(i).getFrom().Label == inEdges.get(0).getLabel())
				  ))
				{
					toBeMono = false;
				}
			}
			if(toBeMono)
			{
				v.Label = inEdges.get(0).getFrom().Label;
				idsToRemoveFromHybrid.add(v.getID());
				monoGraphs[v.Label].addVertex(v);
				continue;
			}
			LinkedList<Edge> outEdges = v.getOutEdges(); 
			toBeMono = (outEdges.size() > 0 && outEdges.get(0).getTo().Label < hybridLabel);
			for(int i = 0; toBeMono && (i < outEdges.size()); i++)
			{
				if(!((outEdges.get(i).getLabel() < hybridLabel)
						&& (outEdges.get(0).getLabel() == outEdges.get(0).getTo().Label) 
						&& (outEdges.get(i).getLabel() == outEdges.get(0).getLabel())
						&& (outEdges.get(i).getTo().Label == outEdges.get(0).getLabel())
				  ))
				{
					toBeMono = false;
				}
			}
			if(toBeMono)
			{
				v.Label = outEdges.get(0).getTo().Label;
				idsToRemoveFromHybrid.add(v.getID());
				monoGraphs[v.Label].addVertex(v);
			}
		}
		for(long l : idsToRemoveFromHybrid)
		{
			monoGraphs[hybridLabel].removeVertex(l);
		}
		
		LinkedList<Vertex> allVs = new LinkedList<Vertex>();
		for(int i = 0; i < monoGraphs.length; i++)
		{
			for(Vertex v : monoGraphs[i].getAllVertexes())
			{
				allVs.add(v);
			}
		}
		*/
		//
		if(Constants.Debug)
		{
			System.out.println("Vertex label assignment");
			for(Vertex v : allVertexes)
			{
				System.out.println("\t ID: " + v.getID() + ", Color: " + v.Label);
			}
		}
		
		
		
		//add the edges to each mono colored graph
		for(int i = 0; i < monoGraphs.length - 1; i++)
		{
			for(Vertex v : monoGraphs[i].getAllVertexes())
			{
				v.Label = i;
				for(Edge e : graph.getVertex(v.getID()).getOutEdges())
				{
					if (vertexToLabel.get(e.getFrom().getID()) == i && vertexToLabel.get(e.getTo().getID()) == i)
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
		//String contractedGraphFileNameBase = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\index\\graph";
		String contractedGraphFileNameBase = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\dc_directed\\index_hybrid\\graph";
		//String contractedGraphFileNameBase = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\dc_undirected\\index_hybrid\\graph";
		
		Graph contractedGraph = null;
		for(int i = 0; i < monoGraphs.length - 1; i++)
		{
			//read graphs from file
			//contractedGraph = GraphFileIO.loadGraph(contractedGraphFileNameBase + i + ".csv", true, false);
			contractedGraph = FloydWarshal.ComputeSPContraction(monoGraphs[i], i);
			//GraphFileIO.saveGraph(contractedGraph, contractedGraphFileNameBase + i + ".csv", false); 
			//contractedGraph = GraphReader.loadGraph(contractedGraphFileNameBase + i + ".csv", true, false);
			
			for(Vertex v : contractedGraph.getAllVertexes())
			{
				v.Label = i;
			}
			
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
				v.Label = i;
				if(!contractedGraph.containsVertex(v.getID()))
				{
					contractedGraph.addVertex(v.getID());
				}
			}
			monoGraphs[i] = contractedGraph;
		}
		
		//combine the separate mono color graphs
		for(int i = 0; i < monoGraphs.length - 1; i++)
		{
			for(Vertex v : monoGraphs[i].getAllVertexes())
			{
				v.Label = i;
				index.IndexGraph.addVertex(v);
			}
		}
		int edgeID = 1;
		for(int i = 0; i < monoGraphs.length - 1; i++)
		{
			for(Edge e : monoGraphs[i].getAllEdges())
			{
				e.setID(edgeID++); //assign new IDs
				index.IndexGraph.addEdge(e);
			}
		}
		//add the multicolor vertexes
		for(Vertex v : monoGraphs[hybridLabel].getAllVertexes())
		{
			v.Label = hybridLabel;
			v.onBridge = false;
			index.IndexGraph.addVertex(v);
		}
		//add the multicolor edges
		for(Edge e : graph.getAllEdges())
		{
			int labelOfFrom = vertexToLabel.get(e.getFrom().getID());
			int labelOfTo = vertexToLabel.get(e.getTo().getID());
			if(labelOfFrom == hybridLabel || labelOfTo == hybridLabel)
			{
				//if(!index.containsEdge(e.getID()))
				{
					index.IndexGraph.addEdge(edgeID++, e.getFrom().getID(), e.getTo().getID(), e.getWeight(), e.getLabel(), true, false);
				}
			}
		}
		
		
		for(Edge e : index.IndexGraph.getAllEdges())
		{
			/*
			if(e.getTo().Label < index.HybridLabel && e.getFrom().Label == index.HybridLabel)
			{
				e.getTo().onBridge = true;
			}
			*/
			if(e.getFrom().Label < index.HybridLabel && e.getTo().Label == index.HybridLabel)
			{
				e.getFrom().onBridge = true;
			}
			
		}
		
		for(Edge e : index.IndexGraph.getAllEdges())
		{
			if(e.getFrom().Label == e.getTo().Label &&  e.getFrom().Label < index.HybridLabel)
			{
				if(e.getTo().onBridge)
				{
					if(!e.getFrom().beforeBridgeEdges.contains(e))
					{
						e.getFrom().beforeBridgeEdges.add(e);
					}
				}
			}
			
			
			if(e.getFrom().onBridge && e.getFrom().Label != e.getTo().Label)
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
		System.out.println("Building hybrid index for " + Constants.GraphFileName + "...");
		DisjointLabelsIndex index = buildIndex(graph);
		//mark the onBridge edges
		int[] labelCount = new int[index.NumberOfLabels];
		int[] bridgeCount = new int[index.NumberOfLabels];
		for(int i = 0; i < labelCount.length; i++)
		{
			labelCount[i] = 0;
			bridgeCount[i] = 0;
		}
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
	}
	
}
