package dedp.algorithms;

import java.util.ArrayList;

import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.MonoContractions;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;

public class FloydWarshal 
{
	public static Graph ComputeSPContraction(Graph graph, int label) throws ObjectNotFoundException, DuplicateEntryException
	{
		int n = (int)graph.getVertexCount();
		Vertex[] vertexes = new Vertex[n];
		graph.getAllVertexes().toArray(vertexes);
		float[][] dist = new float[n][n];
		Graph graphContraction = new Graph();
		//add all the vertexes
		for(Vertex v : vertexes)
		{
			graphContraction.addVertex(v.getID());
		}
		for(int k = -1; k < n; k++)
		{	
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < n; j++)
				{
					if (i == j)
					{
						dist[i][j] = 0;
					}
					else
					{
						if (k == -1)
						{
							dist[i][j] = graph.getEdgeWeight(vertexes[i].getID(), vertexes[j].getID());
						}
						else
						{
							dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
						}
					}
				}
			}
			/*
			System.out.println("At k = " + k);
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < n; j++)
				{
					//System.out.println( i + " --(" + temp[n-1][i][j] + ")--> " +  j);
					//System.out.println( i + " --(" + graph.dist[i][j] + ")--> " +  j);
					System.out.println( vertexes[i].getID() + " --(" + dist[i][j] + ")--> " +  vertexes[j].getID());
					
				}
				System.out.println("");
			}
		*/	
		}
		//build the contraction graph
		long edgeID = 1;
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				if(i == j || dist[i][j] == Float.POSITIVE_INFINITY)
				{
					continue;
				}
				graphContraction.addEdge(edgeID++, vertexes[i].getID(), vertexes[j].getID(), dist[i][j], label, true);
			}
		}
		/*
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				System.out.println( vertexes[i].getID() + " --(" + dist[i][j] + ")--> " +  vertexes[j].getID());
				if(dist[i][j] < Float.POSITIVE_INFINITY)
				{
					System.out.println("Less");
				}
				if(dist[i][j] == Float.POSITIVE_INFINITY)
				{
					System.out.println("Infinity");
				}
			}
			System.out.println("");
		}
		*/
		
		return graphContraction;
	}
	
	
	/*
	public static Graph ComputeSPContraction(Partition partition) throws ObjectNotFoundException, DuplicateEntryException
	{
		int n = (int)partition.getAllVertexes().size();
		PartitionVertex[] vertexes = new PartitionVertex[n];
		partition.getAllVertexes().toArray(vertexes);
		float[][] dist = new float[n][n];
		Graph graphContraction = new Graph();
		//add all the vertexes
		for(PartitionVertex v : vertexes)
		{
			graphContraction.addVertex(v.getId());
		}
		for(int k = -1; k < n; k++)
		{	
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < n; j++)
				{
					if (i == j)
					{
						dist[i][j] = 0;
					}
					else
					{
						if (k == -1)
						{
							dist[i][j] = partition.getEdgeWeight(vertexes[i].getId(), vertexes[j].getId());
						}
						else
						{
							dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
						}
					}
				}
			}
		}
		//build the contraction graph
		long edgeID = 1;
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				if(i == j || dist[i][j] == Float.POSITIVE_INFINITY)
				{
					continue;
				}
				graphContraction.addEdge(edgeID++, vertexes[i].getId(), vertexes[j].getId(), dist[i][j], partition.Label, true);
			}
		}
		return graphContraction;
	}
	*/
	public static void main(String[] args) throws DuplicateEntryException, ObjectNotFoundException 
	{
		Graph graph = new Graph();
		boolean directed = true;
		graph.addEdge(1, 0, 1, 3, 0, directed);
		graph.addEdge(2, 0, 2, 2, 0, directed);
		graph.addEdge(3, 0, 3, 8, 0, directed);
		graph.addEdge(4, 0, 4, 2, 0, directed);
		graph.addEdge(5, 1, 2, 4, 0, directed);
		graph.addEdge(6, 1, 3, 12, 0, directed);
		graph.addEdge(7, 2, 4, 6, 0, directed);
		
		graph.addEdge(8, 1, 0, 3, 0, directed);
		graph.addEdge(9, 2, 0, 2, 0, directed);
		graph.addEdge(10, 3, 0, 8, 0, directed);
		graph.addEdge(11, 4, 0, 2, 0, directed);
		graph.addEdge(12, 2, 1, 4, 0, directed);
		graph.addEdge(13, 3, 1, 12, 0, directed);
		graph.addEdge(14, 4, 2, 6, 0, directed);
		graph.addEdge(15, 7, 8, 6, 0, directed);
		/*
		ArrayList<Integer> labels = new ArrayList<Integer>();
		labels.add(0);
		graph = MonoContractions.buildIndex(graph);
		SPResult result = Dijkstra.shortestDistance(graph, 2, 4, labels);
		System.out.println(result.Distaince + ", " + result.NumberOfExploredEdges);
		*/
		graph = ComputeSPContraction(graph, 0);
		for(Edge e : graph.getAllEdges())
		{
			System.out.println(e.getID() + ", " + e.getFrom().getID() + ", " + e.getTo().getID() + ", " + e.getWeight() + ", " + e.getLabel());
		}
		//System.out.println("0 --> 1 : " + graph.getEdgeWeight(0, 1));
		//System.out.println("1 --> 0 : " + graph.getEdgeWeight(1, 0));
	}

}
