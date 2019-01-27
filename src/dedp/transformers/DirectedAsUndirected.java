package dedp.transformers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DirectedAsUndirected 
{
	
	public static void main(String[] args) throws IOException 
	{
		String inputFile = args[0];
		String outputFile = args[1];
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		writer.write(reader.readLine() + "\n"); ; //read and write the header
		String line = "";
		String[] tempArr = null;
		ArrayList<GraphFileLine> graphFileLines = new ArrayList<GraphFileLine>();
		GraphFileLine graphLine = null;
		HashMap<Pair, ArrayList<String>> map = new HashMap<Pair, ArrayList<String>>();
		Pair pair = null;
		ArrayList<String> labels = null;
		GraphFileLine line2 = null;
		while((line = reader.readLine()) != null)
		{
			tempArr = line.split(",");
			graphLine = new GraphFileLine();
			graphLine.From = tempArr[1].trim();
			graphLine.To = tempArr[2].trim();
			graphLine.Label = tempArr[3].trim();
			if(tempArr.length > 4)
			{
				graphLine.Weight = Float.parseFloat(tempArr[4]);
			}
			else
			{
				graphLine.Weight = 1;
			}
				
			pair = new Pair(Integer.parseInt(graphLine.From), Integer.parseInt(graphLine.To));
			labels = map.get(pair);
			if(labels == null || !labels.contains(graphLine.Label))
			{
				if(labels == null)
				{
					labels = new ArrayList<String>();
					map.put(pair, labels);
				}
				labels.add(graphLine.Label);
				graphFileLines.add(graphLine);
				line2 = new GraphFileLine();
				line2.To = graphLine.From;
				line2.From = graphLine.To;
				line2.Label = graphLine.Label;
				line2.Weight = graphLine.Weight;
				graphFileLines.add(line2);
			}
		}
		/*
		GraphFileLine[] graphFileLinesArr = new GraphFileLine[graphFileLines.size()];
		graphFileLines.toArray(graphFileLinesArr);
		boolean isFound = false;
		for(int i = 0; i < graphFileLinesArr.length; i++)
		{
			isFound = false;
			for(int j = 0; j < graphFileLinesArr.length; j++)
			{
				if (i == j)
				{
					continue;
				}
				if(graphFileLinesArr[i].From.equalsIgnoreCase(graphFileLinesArr[j].To) && graphFileLinesArr[i].To.equalsIgnoreCase(graphFileLinesArr[j].From))
				{
					graphFileLinesArr[i].Label = graphFileLinesArr[j].Label; //ensure labels are equal
					isFound = true;
					break;
				}
			}
			if(!isFound)
			{
				graphLine = new GraphFileLine();
				graphLine.From = graphFileLinesArr[i].To;
				graphLine.To = graphFileLinesArr[i].From;
				graphLine.Label = graphFileLinesArr[i].Label;
				graphLine.Weight = graphFileLinesArr[i].Weight;
				graphFileLines.add(graphLine);
			}
		}
		*/
		//write the file
		int lineID = 0;
		for(GraphFileLine fileLine : graphFileLines)
		{
			writer.write(lineID + "," + fileLine.From + "," + fileLine.To + "," + fileLine.Label + "," + fileLine.Weight + "\n");
			lineID++;
		}
		reader.close();
		writer.close();
		System.out.println("Lines written successfully");
	}

}
