package generators.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class GenerateReachQueriesForAllWin {

	public static int startVertex = 237022;
	public static int numOfEdgesToAdd = 1000;
	public static int label = 35;
	public static String outputFilePath = "";
	
	
	public static void main(String[] args) throws IOException 
	{
		//we will ignore the input weights and set the edge weight to 1
				//output format is edgeID,from,to,label,weight
		if(args.length > 1)
		{
			startVertex = Integer.parseInt(args[0]);
			numOfEdgesToAdd = Integer.parseInt(args[1]);
			label = Integer.parseInt(args[2]);
			outputFilePath = args[3];
		}
		
		BufferedWriter eWriter = new BufferedWriter(new FileWriter(outputFilePath));
		eWriter.write("from,to,label,actual\n");
		int from, to, actual = 0;
		
		for(int i = 0; i < numOfEdgesToAdd; i++)
		{
			from = startVertex;
			to = from + 1;
			eWriter.write(from + "," + to + "," + label + "," + actual + "\n");
			startVertex += 2;
		}
		eWriter.flush();
		eWriter.close();
		System.out.println("Done!");
		
	}

}
