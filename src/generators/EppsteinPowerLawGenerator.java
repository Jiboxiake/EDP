package generators;

/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.io.GraphFileIO;
import dedp.structures.*;

/**
 * Graph generator that generates undirected graphs with power-law degree distributions.
 * @author Scott White
 * @see "A Steady State Model for Graph Power Law by David Eppstein and Joseph Wang"
 */
public class EppsteinPowerLawGenerator {
    private int mNumVertices;
    private int mNumEdges;
    private int mNumIterations;
    private double mMaxDegree;
    private Random mRandom;
    
    public int numOfLabels;

    /**
     * Creates an instance with the specified factories and specifications.
     * @param graphFactory the factory to use to generate the graph
     * @param vertexFactory the factory to use to create vertices
     * @param edgeFactory the factory to use to create edges
     * @param numVertices the number of vertices for the generated graph
     * @param numEdges the number of edges the generated graph will have, should be Theta(numVertices)
     * @param r the number of iterations to use; the larger the value the better the graph's degree
     * distribution will approximate a power-law
     */
    public EppsteinPowerLawGenerator(int numVertices, int numEdges, int r, long seed, int numOfLabels) {
        mNumVertices = numVertices;
        mNumEdges = numEdges;
        mNumIterations = r;
        this.numOfLabels = numOfLabels;
        mRandom = new Random(seed);
    }
    
    public int getEdgeColor(double randomCoin)
    {
    	int label = 0;
    	if(randomCoin <= 0.2)
    	{
    		label = 0;
    	} 
    	else if (randomCoin <= 0.35)
    	{
    		label = 1;
    	}
    	else if (randomCoin <= 0.45)
    	{
    		label = 2;
    	}
    	else if (randomCoin <= 0.50)
    	{
    		label = 3;
    	}
    	else
    	{
    		label = (int)(4 + randomCoin * (numOfLabels - 4));
    	}
    	return label;
    }

    public static float MaxEdgeWeight = 1000;
    public static double bridgeVertexProb = 0.1;
    
    protected Graph initializeGraph() throws ObjectNotFoundException, DuplicateEntryException {
        Graph graph = new Graph();
        for(int i = 0; i < mNumVertices; i++) 
        {
        	graph.addVertex(i);
        	if(mRandom.nextDouble() < bridgeVertexProb)
        	{
        		graph.getVertex((long)i).onBridge = true;
        	}
        }
        List<Vertex> vertices = new ArrayList(graph.getAllVertexes());
        long edgeId = 0, uId = 0, vId = 0;
        int label;
        while (graph.getEdgeCount() < (long)mNumEdges) 
        {
        	uId = (int) (mRandom.nextDouble() * mNumVertices);
            Vertex u = vertices.get((int)uId);
            vId = (int) (mRandom.nextDouble() * mNumVertices);
            Vertex v = vertices.get((int)vId);
            if (!graph.directedEdgeExists(uId, vId))
            {
            	label = mRandom.nextInt(this.numOfLabels);
            	if(!u.onBridge)
            	{
            		List<Edge> outEdges = u.getOutEdges();
            		if(outEdges.size() > 0)
            		{
            			label = outEdges.get(0).getLabel();
            		}
            	}
                graph.addEdge(edgeId++, uId, vId, mRandom.nextFloat() * MaxEdgeWeight, label, true, false);
            }
        }

        double maxDegree = 0;
        for (Vertex v : graph.getAllVertexes()) {
            maxDegree = Math.max(v.fanOut(),maxDegree);
        }
        mMaxDegree = maxDegree; //(maxDegree+1)*(maxDegree)/2;

        return graph;
    }

    /**
     * Generates a graph whose degree distribution approximates a power-law.
     * @return the generated graph
     * @throws DuplicateEntryException 
     * @throws ObjectNotFoundException 
     */
    public Graph create() throws ObjectNotFoundException, DuplicateEntryException {
        Graph graph = initializeGraph();

        List<Vertex> vertices = new ArrayList(graph.getAllVertexes());
        for (int rIdx = 0; rIdx < mNumIterations; rIdx++) {

            Vertex v = null;
            int degree = 0;
            do 
            {
                v = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
                degree = v.fanOut();

            } while (degree == 0);

            //List<Edge> edges = new ArrayList<E>(graph.getIncidentEdges(v));
            List<Edge> edges = v.getOutEdges();
            Edge randomExistingEdge = edges.get((int) (mRandom.nextDouble()*degree));

            // FIXME: look at email thread on a more efficient RNG for arbitrary distributions
            
            Vertex x = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            Vertex y = null;
            do {
                y = vertices.get((int) (mRandom.nextDouble() * mNumVertices));

            } while (mRandom.nextDouble() > ((y.fanOut()+1)/mMaxDegree));

            if (!graph.directedEdgeExists(x.getID(), y.getID()) && x.getID() != y.getID()) 
            {
            	graph.removeEdge(randomExistingEdge);
            	graph.addEdge(randomExistingEdge.getID(), x.getID(), y.getID(), randomExistingEdge.getWeight(), randomExistingEdge.getLabel(), true, false);
            }
        }

        return graph;
    }

    /**
     * Sets the seed for the random number generator.
     * @param seed input to the random number generator.
     */
    public void setSeed(long seed) {
        mRandom.setSeed(seed);
    }
    
    public static void main(String[] args) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException 
	{
    	//D:\\Research\\Datasets\\random\\graph1.csv 1000000 2000000 10 10000 1234567
		//read the tiger file, create a memory graph, write the memory graph
    	String outputFile = args[0];
    	int numOfVertexes = Integer.parseInt(args[1]);
    	int numOfEdges = Integer.parseInt(args[2]);
    	int numOfLabels = Integer.parseInt(args[3]);
    	int numOfIterations = Integer.parseInt(args[4]);
    	int seed = Integer.parseInt(args[5]);
    	
    	EppsteinPowerLawGenerator generator = new EppsteinPowerLawGenerator(numOfVertexes, numOfEdges, numOfIterations, seed, numOfLabels);
		Graph graph = generator.create();
		//print some statistics
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