package generators.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;


public class QueryIO 
{
	public static void writeQueryFile(ArrayList<Query> lstQueries, String queryFileName, boolean writeHeader, boolean append) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		System.out.println("Writing " + lstQueries.size() + " into " + queryFileName);
		//read edges and add vertexes automatically
		BufferedWriter writer = new BufferedWriter(new FileWriter(queryFileName, append));
		String line = "";
		String labels = "";
		if(!append && writeHeader)
		{
			writer.write( "source,destination,cost,pathlength\n");
		}
		for(Query query : lstQueries)
		{
			labels = "";
			if(query.Labels != null)
			{
				for(int label : query.Labels)
				{
					labels += (label + ";");
				}
			}
			line = query.Source + "," + query.Destination + "," + query.Cost  + "," + query.PathLength;
			if(!labels.isEmpty())
			{
				line += "," + labels;
			}
			line += "\n";
			writer.write(line);
		}
		writer.close();
	}
	
	public static ArrayList<Query> readQueryFile(String queryFileName, boolean readHeader) throws IOException, NumberFormatException, DuplicateEntryException, ObjectNotFoundException
	{
		ArrayList<Query> lstQueries = new ArrayList<Query>();
		Query query = null;
		BufferedReader reader = new BufferedReader(new FileReader(queryFileName));
		String line = "";
		String[] temp = null;
		if(readHeader)
		{
			reader.readLine(); //consue the header "queryId,source,destination,cost,pathlength"
		}
		String[] labelsSplit = null;
		while((line = reader.readLine()) != null)
		{
			temp = line.split(",");
			query = new Query();
			query.Source = Integer.parseInt(temp[0].trim());
			query.Destination = Integer.parseInt(temp[1].trim());
			query.Cost = Float.parseFloat(temp[2].trim());
			query.PathLength = Integer.parseInt(temp[3].trim());
			query.Labels = new ArrayList<Integer>();
			if(temp.length > 4)
			{
				labelsSplit = temp[4].trim().split(";");
				for(String lbl : labelsSplit)
				{
					if(!lbl.trim().isEmpty())
					{
						query.Labels.add(Integer.parseInt(lbl));
					}
				}
				Collections.sort(query.Labels);
			}
			
			lstQueries.add(query);
		}
		reader.close();
		return lstQueries;
	}
}
