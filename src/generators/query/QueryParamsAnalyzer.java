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

public class QueryParamsAnalyzer 
{
	public static void main(String[] args) throws Exception
	{
		if(args.length > 0)
		{
			Helper.setParametersFromFile(args[0]);
			Helper.printParamValues();
		}
		//read the query file
		System.out.println("Start reading queries...");
		ArrayList<Query> lstQueries = QueryIO.readQueryFile(QueryGenerator.getQueryFileName(), false);
		System.out.println("We've read " + lstQueries.size() + " query.");
		//count num of labels
		int maxLabels = 40;
		Integer[] lblHistogram = new Integer[maxLabels];
		for(int i = 0; i < maxLabels; i++)
		{
			lblHistogram[i] = 0;
		}
		int minPathLength = Integer.MAX_VALUE, maxPathLength = Integer.MIN_VALUE;
		for(Query q : lstQueries)
		{
			
			if(q.Labels != null && q.PathLength > 500 && q.PathLength < 600)
			{
				lblHistogram[q.Labels.size()]++;
			}
			if(q.PathLength > maxPathLength)
				maxPathLength = q.PathLength;
			if(q.PathLength < minPathLength)
				minPathLength = q.PathLength;
		}
		System.out.println("Min PathLength: "+ minPathLength);
		System.out.println("Max PathLength: "+ maxPathLength);
		for(int i = 0; i < maxLabels; i++)
		{
			System.out.println("With labels of size " + i + ": " + lblHistogram[i]);
		}
	}
	
	public static String getQueryFileName(int labelId)
	{
		return Constants.QueryFileBaseName + "_" + labelId + ".csv";
	}
}
